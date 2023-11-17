package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    /**
     * <h2>V1 : Entity 를 직접 노출</h2>
     * <p>
     *     Lazy 로딩이기 때문에 관련된 엔티티를 Lazy 강제 초기화로 뿌려준다.
     *     Entity 를 직접 노출하는건 매우 안좋은 방법
     * </p>
     *
     */
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1(){
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName();
            order.getDelivery().getAddress();
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o->o.getItem().getName());
        }

        return all;
    }

    /**
     * <h2> V2 : DTO 로 반환</h2>
     * <p>
     *     엔티티가 아닌 API 스펙에 맞춘 필요한 데이터만 노출하는 DTO 를 반환한다.
     *     DTO 안에 있는 엔티티 또한 추가로 DTO 로 변환하여야 한다(Order -> OrderItem)
     * </p>
     * @return
     */
    @GetMapping("/api/v2/orders")
    public Result<?> ordersV2(){
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> collect = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());
        return new Result<>(collect.size(), collect);
    }

    /**
     * <h2>V3 : 패치조인으로 성능 최적화</h2>
     * <li>패치조인으로 여러 엔티티를 한번에 조회해서 SQL 쿼리는 딱 1번나감</li>
     * <li>단, Order와 OrderItem 은 1대N 관계이므로 OrderItem에는 똑같은 Order 가 2개씩 있으므로 데이터 중복이 일어난다.</li>
     * <li>OrderItem 에는 Id가 4인 컬럼 2개 Id가 11인 컬럼 2개가 있으므로 Order 와 OrderItem 을 Join 하면 4개의 컬럼이 생긴다.</li>
     * <li>JPA 에서 제공하는 Distinct 를 사용해서 에플리케이션에서 중복을 걸러준다. -> Order 가 컬렉션 페치 조인 때문에 중복조회되는것을 막아준다.</li>
     * <li> 가장 큰 단점은 <b>페이징 불가능</b></li>
     *
     */
    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3(){
        List<Order> orders = orderRepository.findAllWithItem();

        return orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());
    }

    /**
     * <h2>V3.1 : 페이징과 한계돌파</h2>
     * <li>컬렉션 패치조인을 하면 페이징이 안되기 때문에 컬렉션이 아닌 엔티티와 패치조인을 한다.</li>
     * <li>BatchSize 를 지정하여 한꺼번에 데이터를 가져오기 때문에 쿼리도 줄어든다.</li>
     */
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_page(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "offset", defaultValue = "100") int limit){
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);

        return orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());
    }

    /**
     * <h2>V4 : DTO로 직접 조회</h2>
     * <li>JPQL 에서 new 키워드를 사용하여 직접 DTO 를 조회하였다.</li>
     * <p>
     *     하지만 컬렉션 연관관계 때문에 JPQL 에서 리스트인 컬렉션을 집어넣을 수 없기 때문에 컬렉션 컬럼을 제외하고 select 한 뒤에
     *     다시 컬렉션인 부분을 for-each 돌려서 채워주었다.
     * </p>
     *
     * @return
     */
    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4(){
        return orderQueryRepository.findByOrderQueryDtos();
    }

    @Data
    static class OrderDto{

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();;
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            orderItems = order.getOrderItems().stream()
                    .map(o -> new OrderItemDto(o))
                    .collect(Collectors.toList());
        }
    }

    @Data
    static class OrderItemDto{

        private String itemName; // 상품명
        private int orderPrice; // 주문 가격
        private int count; // 주문 수량

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }

    @Data
    @AllArgsConstructor
    static class Result<T>{
        private int count;
        private T data;
    }
}
