package ichen.chu.drawnsend;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SampleTest {
    @BeforeClass
    public static void beforeClass() throws Exception {
        System.out.println("beforeClass");
    }

    @Before
    public void setUp() throws Exception {
        System.out.println("Before");
    }

    @Test
    public void test1(){
        System.out.println("test1");
    }

    @Test
    public void test2(){
        System.out.println("test2");
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("After");
    }

    @AfterClass
    public static void afterClass() throws Exception{
        System.out.print("AfterClass");
    }
}
