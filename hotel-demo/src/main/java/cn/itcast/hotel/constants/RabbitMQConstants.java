package cn.itcast.hotel.constants;

/**
 * @Description mq常量
 * @Date 2023-05-29-15-32
 * @Author qianzhikang
 */
public class RabbitMQConstants {
    /** 交换机名称 */
    public final static String HOTEL_EXCHANGE = "hotel.topic";
    /** 监听修改和新增的队列 */
    public final static String HOTEL_INSERT_QUEUE= "hotel.insert.queue";
    /** 监听删除的队列 */
    public final static String HOTEL_DELETE_QUEUE= "hotel.delete.queue";
    /** 新增或修改的routing-key */
    public final static String HOTEL_INSERT_KEY= "hotel.insert";
    /** 删除的routing-key */
    public final static String HOTEL_DELETE_KEY= "hotel.delete";
}
