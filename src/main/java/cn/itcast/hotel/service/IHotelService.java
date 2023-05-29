package cn.itcast.hotel.service;

import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.PageResult;
import cn.itcast.hotel.pojo.RequestParams;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface IHotelService extends IService<Hotel> {
    /**
     * 查询酒店列表
     * @param requestParams 查询参数
     * @return 分页结果
     */
    PageResult search(RequestParams requestParams);


    /**
     *
     * @return
     */
    Map<String, List<String>> filters(RequestParams requestParams);
}
