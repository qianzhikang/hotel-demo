package cn.itcast.hotel.pojo;

import lombok.Data;

/**
 * @Description 查询参数
 * @Date 2023-05-26-11-14
 * @Author qianzhikang
 */
@Data
public class RequestParams {
    private String key;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String city;
    private String brand;
    private String starName;
    private Integer minPrice;
    private Integer maxPrice;
    private String location;
}