package cn.itcast.hotel.mq;

import cn.itcast.hotel.constants.RabbitMQConstants;
import cn.itcast.hotel.service.IHotelService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Description TODO
 * @Date 2023-05-29-16-07
 * @Author qianzhikang
 */
@Component
public class HotelListener {

    @Resource
    private IHotelService iHotelService;

    @RabbitListener(bindings = @QueueBinding(value = @Queue(name = RabbitMQConstants.HOTEL_INSERT_QUEUE),
            exchange = @Exchange(name = RabbitMQConstants.HOTEL_EXCHANGE, type = ExchangeTypes.DIRECT),
            key = RabbitMQConstants.HOTEL_INSERT_KEY))
    public void hotelInsertOrUpdateListener(Long id){
        iHotelService.insertById(id);
    }


    @RabbitListener(bindings = @QueueBinding(value = @Queue(name = RabbitMQConstants.HOTEL_DELETE_QUEUE),
            exchange = @Exchange(name = RabbitMQConstants.HOTEL_EXCHANGE, type = ExchangeTypes.DIRECT),
            key = RabbitMQConstants.HOTEL_DELETE_KEY))
    public void hotelDeleteListener(Long id){
        iHotelService.deleteById(id);
    }
}
