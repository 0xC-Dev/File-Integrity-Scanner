package org.rtt;

import java.io.*;
import java.security.MessageDigest;

import java.nio.file.*;
import java.util.*;

public class FileIntegrityVerifier {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage:");
            System.out.println("  -g / --generate <folder>");
            System.out.println("  -v / --verify <folder> <pathToChecksumFile>");
            return;
        }

        try {
            if (args[0].equals("-g") || args[0].equals("--generate")) {
                String folderPath = args[1];
                generateChecksum(folderPath);
            } else if (args[0].equals("-v") || args[0].equals("--verify")) {
                if (args.length < 3) {
                    System.out.println("Please provide a folder and a checksum file for verification.");
                    return;
                }
                String folderPath = args[1];
                String checksumFilePath = args[2];
                verifyChecksum(folderPath, checksumFilePath);
            } else {
                System.out.println("Invalid flag. Use -g/--generate or -v/--verify.");
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void generateChecksum(String folderPath) throws Exception {
        List<FileData> fileDataList = new ArrayList<>();
        Path folder = Paths.get(folderPath);

        if (!Files.isDirectory(folder)) {
            throw new IllegalArgumentException("Provided path is not a folder.");
        }

        Files.walk(folder).filter(Files::isRegularFile).forEach(file -> {
            try {
                String hash = calculateHash(file.toFile());
                long size = Files.size(file);
                String name = file.toString();
                fileDataList.add(new FileData(name, size, hash));
            } catch (Exception e) {
                System.err.println("Error processing file: " + file + " -> " + e.getMessage());
            }
        });

        Path checksumFile = folder.resolve("checksum.txt");
        try (BufferedWriter writer = Files.newBufferedWriter(checksumFile)) {
            for (FileData data : fileDataList) {
                writer.write(data.toString());
                writer.newLine();
            }
        }

        System.out.println("Checksum file generated: " + checksumFile);
    }

    private static void verifyChecksum(String folderPath, String checksumFilePath) throws Exception {
        Path folder = Paths.get(folderPath);
        Path checksumFile = Paths.get(checksumFilePath);

        if (!Files.isDirectory(folder)) {
            throw new IllegalArgumentException("Provided folder path is not valid.");
        }

        if (!Files.exists(checksumFile)) {
            throw new IllegalArgumentException("Checksum file not found.");
        }

        Map<String, FileData> checksumData = new HashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(checksumFile)) {
            String line;
            while ((line = reader.readLine()) != null) {
                FileData data = FileData.fromString(line);
                checksumData.put(data.name, data);
            }
        }

        Files.walk(folder).filter(Files::isRegularFile).forEach(file -> {
            try {
                String name = file.toString();
                long size = Files.size(file);
                String hash = calculateHash(file.toFile());

                FileData expected = checksumData.get(name);
                if (expected == null) {
                    System.out.println("New file detected: " + name);
                } else if (!expected.hash.equals(hash) || expected.size != size) {
                    System.out.println("Mismatch detected: " + name);
                }
            } catch (Exception e) {
                System.err.println("Error processing file: " + file + " -> " + e.getMessage());
            }
        });

        System.out.println("Verification complete.");
    }

    private static String calculateHash(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }
        StringBuilder hexString = new StringBuilder();
        for (byte b : digest.digest()) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    static class FileData {
        String name;
        long size;
        String hash;

        FileData(String name, long size, String hash) {
            this.name = name;
            this.size = size;
            this.hash = hash;
        }

        @Override
        public String toString() {
            return name + "|" + size + "|" + hash;
        }

        static FileData fromString(String data) {
            String[] parts = data.split("\\|");
            return new FileData(parts[0], Long.parseLong(parts[1]), parts[2]);
        }
    }
}
