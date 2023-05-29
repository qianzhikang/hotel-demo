package cn.itcast.hotel.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.lucene.util.CollectionUtil;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
public class HotelDoc {
    private Long id;
    private String name;
    private String address;
    private Integer price;
    private Integer score;
    private String brand;
    private String city;

    private String starName;

    private String business;
    private String location;
    private String pic;

    private Object distance;

    private Boolean isAD;
    /** 自动补全内容 */
    private List<String> suggestion;
    public HotelDoc(Hotel hotel) {
        this.id = hotel.getId();
        this.name = hotel.getName();
        this.address = hotel.getAddress();
        this.price = hotel.getPrice();
        this.score = hotel.getScore();
        this.brand = hotel.getBrand();
        this.city = hotel.getCity();
        this.starName = hotel.getStarName();
        this.business = hotel.getBusiness();
        this.location = hotel.getLatitude() + ", " + hotel.getLongitude();
        this.pic = hotel.getPic();
        // business有多个值的情况
        if (this.business.contains("/")){
            String[] split = this.business.split("/");
            this.suggestion = new ArrayList<>();
            this.suggestion.add(this.brand);
            // 添加所有的split
            Collections.addAll(this.suggestion,split);
        }else {
            this.suggestion = Arrays.asList(this.brand,this.business);
        }
    }
}
