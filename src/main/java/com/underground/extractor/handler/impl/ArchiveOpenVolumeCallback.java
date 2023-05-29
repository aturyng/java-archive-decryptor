package com.underground.extractor.handler.impl;

import net.sf.sevenzipjbinding.IArchiveOpenVolumeCallback;
import net.sf.sevenzipjbinding.IInStream;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

public class ArchiveOpenVolumeCallback implements IArchiveOpenVolumeCallback {

        /**
         * Cache for opened file streams
         */
        private final Map<String, RandomAccessFile> openedRandomAccessFileList = new HashMap<>();

        /**
         * This method doesn't needed, if using with VolumedArchiveInStream
         * and pass the name of the first archive in constructor.
         * (Use two argument constructor)
         *
         * @see IArchiveOpenVolumeCallback#getProperty(PropID)
         */
        public Object getProperty(PropID propID) {
            return null;
        }

        /**
         * The name of the required volume will be calculated out of the
         * name of the first volume and volume index. If you need
         * volume index (integer) you will have to parse filename
         * and extract index.
         *
         * <pre>
         * int index = filename.substring(filename.length() - 3,
         *         filename.length());
         * </pre>
         */
        public IInStream getStream(String filename) {
            try {
                // We use caching of opened streams, so check cache first
                RandomAccessFile randomAccessFile = openedRandomAccessFileList
                        .get(filename);
                if (randomAccessFile != null) { // Cache hit.
                    // Move the file pointer back to the beginning
                    // in order to emulating new stream
                    randomAccessFile.seek(0);
                    return new RandomAccessFileInStream(randomAccessFile);
                }

                // Nothing useful in cache. Open required volume.
                randomAccessFile = new RandomAccessFile(filename, "r");

                // Put new stream in the cache
                openedRandomAccessFileList.put(filename, randomAccessFile);

                return new RandomAccessFileInStream(randomAccessFile);
            } catch (FileNotFoundException fileNotFoundException) {
                // Required volume doesn't exist. This happens if the volume:
                // 1. never exists. 7-Zip doesn't know how many volumes should
                //    exist, so it have to try each volume.
                // 2. should be there, but doesn't. This is an error case.

                // Since normal and error cases are possible,
                // we can't throw an error message
                return null; // We return always null in this case
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

}