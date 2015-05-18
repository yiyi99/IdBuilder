/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myidtest;

import MySeqIdBuilder.SeqIdBuilder;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author yiyi
 */
public class MyIdTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
       MyIdTest test = new MyIdTest();
       test.idtest();
    }
    
    public void idtest(){ 
        final SeqIdBuilder idb = new SeqIdBuilder(1); //节点标识:1
        final CyclicBarrier cdl = new CyclicBarrier(100); 

        for(int i = 0; i < 100; i++){ 
            new Thread(new Runnable() { 
                @Override 
                public void run() { 
                try { 
                    cdl.await(); 
                } catch (InterruptedException e) { 
                    e.printStackTrace(); 
                } catch (BrokenBarrierException e) { 
                    e.printStackTrace(); 
                } 
                //随机生成测试的域标识
                long domainid = ((long)Math.random()) % 32;
                System.out.println(idb.nextId(domainid));} 
             }).start(); 
        } 
        try { 
            TimeUnit.SECONDS.sleep(5); 
        } catch (InterruptedException e) { 
           e.printStackTrace(); 
        } 

    } 
}
