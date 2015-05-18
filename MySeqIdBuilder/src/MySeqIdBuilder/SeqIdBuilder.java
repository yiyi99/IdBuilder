package MySeqIdBuilder;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * 基于Twitter的SnowFlake算法
 * @author yiyi
 */
public class SeqIdBuilder {
    //基准时间戳数值(毫秒,2015-01-01 00:00)
    private final static long twepoch = 1420041611023L;
    //域标识的位长 
    private final static long domainIdBits = 5L;
    //域标识的最大值 
    private final static long maxDomainId = -1L ^ (-1L << domainIdBits);
    //节点标识的位长 
    private final static long nodeIdBits = 7L;
    //节点标识的最大值 
    private final static long maxNodeId = -1L ^ (-1L << nodeIdBits);
    //毫秒内自增序列值的位长 
    private final static long sequenceBits = 10L;
    //域标识左偏移10位 
    private final static long domainIdShift = sequenceBits;
    //节点标识左偏移15位 
    private final static long nodeIdShift = sequenceBits + domainIdBits;
    //时间毫秒左偏移22位 
    private final static long timestampLeftShift = sequenceBits + domainIdBits + nodeIdBits;

    private final static long sequenceMask = -1L ^ (-1L << sequenceBits);

    private static long lastTimestamp = -1L;

    private long sequence = 0L;
    private final long nodeId;
    
    /*
    NodeId:  节点标识
    */
    public SeqIdBuilder(long NodeId) {
        if (NodeId > maxNodeId || NodeId < 0) { //节点标识的值错误
            throw new IllegalArgumentException("Node Id can't be greater than " + maxNodeId + " or less than 0");
        }
        
        this.nodeId = NodeId;
    }

    /*
    domainId: 域标识
    */
    public synchronized long nextId(long domainId) {
        if (domainId > maxDomainId || domainId < 0) { //域标识的值错误
            try {
                throw new IllegalArgumentException("Domain Id can't be greater than " + maxDomainId + " or less than 0");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        long timestamp = GetTime();
        if (timestamp < lastTimestamp) { //系统时间调整错误，实际应用中可以考虑将该值保存到redis，这样可以保证重启应用不会丢失该数值
            try {
                throw new Exception("Clock moved backwards.  Refusing to generate id for " + (lastTimestamp - timestamp) + " milliseconds");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (lastTimestamp == timestamp) { 
            //申请在同一毫秒内，则序列值加1 
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) { 
                //同一毫秒内序列值已达最大值，则等待下一毫秒重新计算 
                timestamp = WaitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0;
        }
        lastTimestamp = timestamp;
        
        //生成ID，并返回 
        long nextId = ((timestamp - twepoch) << timestampLeftShift)
                | (nodeId << nodeIdShift)
                | (domainId << domainIdShift)
                | sequence;

        return nextId;
    }

    private long WaitNextMillis(final long lastTimestamp) {
        long timestamp = this.GetTime();
        while (timestamp <= lastTimestamp) {
            timestamp = this.GetTime();
        }
        return timestamp;
    }

    private long GetTime() {
        return System.currentTimeMillis();
    }
}
