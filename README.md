# java-archive-decryptor
Java library to handle all most popular achive formats like zip, 7z and rar. Supports batch extracting of multiple files with multiple passwords.

Example usage:

extract --inDir ./all_archives --outDir ./dir_to_extract_to --pwFile passwords.txt --rem true

All archives in the directory "all_archives" are going to be extracted to directory "dir_to_extract_to" using passwords for the "passwrods.txt" file. Since "rem" is set to "true", the archives are deleted after successful extraction. 
