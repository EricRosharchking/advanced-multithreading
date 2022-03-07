package com.liyuan.hong.advancedmultithreading;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;

/**
 * Create CLI application that scans a specified folder and provides detailed statistics:
 * <p>
 * File count.
 * Folder count.
 * Size (sum of all files size) (similar to Windows context menu Properties). Since the folder
 * may contain a huge number of files the scanning process should be executed in a separate
 * thread displaying an informational message with some simple animation like the progress bar
 * in CLI (up to you, but I'd like to see that task is in progress).
 * Once the task is done, the statistics should be displayed in the output immediately.
 * Additionally, there should be the ability to interrupt the process by pressing some reserved
 * key (for instance c). Of course, use Fork-Join Framework for implementation parallel scanning.
 */
public class FileScannerFJP {
    public static void main(String[] args) {
        if (args.length == 1) {
            String filePath = args[0];
            FileScanner task = new FileScanner(filePath);
            if (!Files.isDirectory(FileSystems.getDefault().getPath(filePath))) {
                System.out.println("Please provide a path to a folder");
                return;
            }
            Map<String, Long> res = task.invoke();
            System.out.println(res);
        } else {
            System.out.println("Required args ONE, found args [" + args.length +"]");
        }
    }
}

class FileScanner extends RecursiveTask<Map<String, Long>> {
    private String filePath;
    private Path path;
    private Map<String, Long> fileAndFolders;
    private final String FOLDERS = "Folders";
    private final String FILES = "Files";
    private final String SIZE = "Size";

    public FileScanner(String filePath) {
        this.filePath = filePath;
        if (Files.isDirectory(FileSystems.getDefault().getPath(filePath))) {
            path = FileSystems.getDefault().getPath(filePath);
        }
        fileAndFolders = new HashMap<>();
        fileAndFolders.put(FOLDERS, 0L);
        fileAndFolders.put(FILES, 0L);
        fileAndFolders.put(SIZE, 0L);
    }

    @Override
    protected Map<String, Long> compute() {

        long fileCount = 0L, folderCount = 0L, size = 0L;
        List<FileScanner> listOfScanners = new ArrayList<>();
//        List<Map<String, Long>> listOfMaps = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path subPath : stream) {
                if (!Files.isDirectory(subPath)) {
                    size += Files.size(subPath);
                    fileCount += 1;
                } else {
                    folderCount += 1;
//                    listOfMaps.add(new FileScanner(subPath.toString()).invoke());
                    FileScanner fileScanner = new FileScanner(subPath.toString());
                    fileScanner.fork();
                    listOfScanners.add(fileScanner);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        fileAndFolders.put(FOLDERS, folderCount);
        fileAndFolders.put(FILES, fileCount);
        fileAndFolders.put(SIZE, size);
//        for (Map<String, Long> map: listOfMaps) {
//            merge(map);
//        }
        for (FileScanner fileScanner : listOfScanners) {
            merge(fileScanner.join());
        }
        return fileAndFolders;
    }

    private void merge(Map<String, Long> subFolders) {
        fileAndFolders.put(FOLDERS, fileAndFolders.get(FOLDERS) + subFolders.get(FOLDERS));
        fileAndFolders.put(FILES, fileAndFolders.get(FILES) + subFolders.get(FILES));
        fileAndFolders.put(SIZE, fileAndFolders.get(SIZE) + subFolders.get(SIZE));
    }

    /*
     void run() {
         if (path != null) {
             try {
                 Files.newDirectoryStream(path, Files::isDirectory).forEach(subPath -> {
                     if (!Files.isDirectory(subPath)) {
                         try {
                             System.out.println(subPath.getFileName().toString() + ":" + Files.size(subPath) + " bytes");
                         } catch (IOException e) {
                             e.printStackTrace();
                         }
                     } else {
                         System.out.println(subPath.toString() + "is a folder");
                         Thread t = new Thread(new FileScanner(subPath.toString()));
                         t.start();
                     }
                 });
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
     */

}
