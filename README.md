# File Integrity Verifier POC

This Java program is designed to help verify the integrity of files within a specified folder by generating and verifying SHA-256 checksums. It provides two main functionalities: generating a checksum file and verifying the integrity of files based on that file.

## Features

- **Generate Checksum File**: Creates a checksum file for all files in a folder.
- **Verify File Integrity**: Compares the current files in a folder with an existing checksum file to detect any changes or new files.

## Usage

### Generate Checksum File

To generate a checksum file for a folder, use the following command:

`java -jar FileIntegrityVerifier.jar -g <folder_path>`

### Verify File Integrity

To verify the integrity of files against an existing checksum file, use:

`java -jar FileIntegrityVerifier.jar -v <folder_path> <checksum_file_path>`

## Requirements

- Java 8 or higher.

## Example

### Generate Checksums:

`java -jar FileIntegrityVerifier.jar -g /path/to/folder`

This will create a `checksum.txt` file inside the specified folder with the checksums, file sizes, and names of all files.

### Verify Files:

`java -jar FileIntegrityVerifier.jar -v /path/to/folder /path/to/checksum.txt`

This will verify the files in the specified folder against the provided checksum file and output any mismatches or new files.

### TODO:
- Will add better logging
- Improve performance
- Live monitor mode
