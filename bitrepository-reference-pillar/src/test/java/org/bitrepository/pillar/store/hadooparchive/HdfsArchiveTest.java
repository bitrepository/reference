package org.bitrepository.pillar.store.hadooparchive;

import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.hdfs.HdfsConfiguration;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static org.testng.Assert.*;

public class HdfsArchiveTest {
    
    public static final String COLLECTION_ID = "collectionID";
    public static final Path ROOT_PATH = new Path("/");
    
    @Test
    /**
     * https://wiki.apache.org/hadoop/HowToDevelopUnitTests
     */
    public void testHasFile() throws IOException {
    
        HdfsConfiguration conf = new HdfsConfiguration();
        
        File baseDir = new File("./target/hdfs/" + "testHasFile").getAbsoluteFile();
        
        FileUtil.fullyDelete(baseDir);
        conf.set(MiniDFSCluster.HDFS_MINIDFS_BASEDIR, baseDir.getAbsolutePath());
        MiniDFSCluster.Builder builder = new MiniDFSCluster.Builder(conf);
        try(MiniDFSCluster hdfsCluster = builder.build()) {
            String hdfsURI = "hdfs://localhost:" + hdfsCluster.getNameNodePort() + "/";
    
            DistributedFileSystem fileSystem = hdfsCluster.getFileSystem();
    
            Path testFile = new Path(ROOT_PATH,
                                     new Path(COLLECTION_ID,
                                              new Path(
                                                      HdfsArchive.FILE_PATH_NAME,
                                                      "testFile")));
            assertTrue(fileSystem.createNewFile(testFile));
            
            assertTrue(fileSystem.exists(testFile));
            
            HdfsArchive archive = new HdfsArchive(COLLECTION_ID,
                                                  fileSystem,
                                                  ROOT_PATH);
            
            
            assertTrue(archive.hasFile(testFile.getName()));
        }
        
        
    }
}