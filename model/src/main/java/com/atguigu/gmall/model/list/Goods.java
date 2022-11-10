package com.atguigu.gmall.model.list;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.List;

// Index = goods , Type = info  es 7.8.0 逐渐淡化type！  修改！

/**
 * Document:文档
 * indexName：指定索引库
 * shards：分片存储 3
 * replicas：副本分片2
 *  一条数据可以存到几个分片  1  3  2    3 *3=9分片
 */
/**
 * String :
 *       text:分词
 *        keyword：不分词
 */
@Data
@Document(indexName = "goods" , shards = 3,replicas = 2)
public class Goods {
    // 商品Id skuId
    @Id
    private Long id;
    @Field(type = FieldType.Keyword, index = false)
    private String defaultImg;
    //  es 中能分词的字段，这个字段数据类型必须是 text！keyword 不分词！
    @Field(type = FieldType.Text, analyzer = "ik_max_word",searchAnalyzer ="ik_smart" )
    private String title;
    @Field(type = FieldType.Double)
    private Double price;
    //  @Field(type = FieldType.Date)   6.8.1
    @Field(type = FieldType.Date,format = DateFormat.custom,pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime; // 新品
    @Field(type = FieldType.Long)
    private Long tmId;
    @Field(type = FieldType.Keyword)
    private String tmName;
    @Field(type = FieldType.Keyword)
    private String tmLogoUrl;
    @Field(type = FieldType.Long)
    private Long category1Id;
    @Field(type = FieldType.Keyword)
    private String category1Name;
    @Field(type = FieldType.Long)
    private Long category2Id;
    @Field(type = FieldType.Keyword)
    private String category2Name;
    @Field(type = FieldType.Long)
    private Long category3Id;
    @Field(type = FieldType.Keyword)
    private String category3Name;
    @Field(type = FieldType.Long)
    private Long hotScore = 0L;
    // Nested 支持嵌套查询
    @Field(type = FieldType.Nested)
    private List<SearchAttr> attrs;

}
