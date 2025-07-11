package javamid.vitrina.app.repositories;


import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

@Repository  //repository for products table for access to image as byte[]
public class ProductImageRepository {
  private final DatabaseClient dbClient;
  public ProductImageRepository( DatabaseClient dbClient ){ this.dbClient = dbClient; }

  public Mono<byte[]> findImageById(Long id) {
    return dbClient.sql("SELECT image FROM products WHERE id = :id")
            .bind("id", id)
            .map((row, metadata) -> {
              ByteBuffer buffer = row.get("image", ByteBuffer.class);
              return buffer != null ? buffer.array() : new byte[0];
            })
            .one();
  }

  public Mono<byte[]> findOrderItemImageById(Long id) {
    return dbClient.sql("SELECT image FROM order_items WHERE id = :id")
            .bind("id", id)
            .map((row, metadata) -> {
              ByteBuffer buffer = row.get("image", ByteBuffer.class);
              return buffer != null ? buffer.array() : new byte[0];
            })
            .one();
  }
}