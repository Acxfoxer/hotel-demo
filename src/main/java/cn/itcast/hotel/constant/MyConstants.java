package cn.itcast.hotel.constant;

public class MyConstants {
    /**
     * 交换机
     */
    public final static String HOTEL_EXCHANGE="hotel.topic";
    /**
     * 监听新增和修改的队列
     */
    public final static String HOTEL_INSERT_QUEUE="hotel.insert.queue";
    /**
     * 监听删除的队列
     */
    public final static String HOTEL_DELETE_QUEUE="hotel.delete.queue";
    /**
     * 新增和修改队列的RootingKey
     */
    public final static String HOTEL_INSERT_KEY="hotel.insert";
    /**
     * 删除队列的RootingKey
     */
    public final static String HOTEL_DELETE_KEY="hotel.delete";

}
