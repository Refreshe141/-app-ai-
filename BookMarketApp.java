
import java.util.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;
import java.util.logging.Level;


// 사용자 역할 정의
enum UserRole {
    ADMIN, CUSTOMER
}

// 회원 등급 정의
enum MembershipLevel {
    NORMAL, SILVER, GOLD, PLATINUM;
}

// 도서 리뷰 클래스 (Review 클래스로 변경)
class Review implements Serializable { // BookMarket 대신 Review로 클래스명 수정
    private static final long serialVersionUID = 1L;
    private String username;
    private int rating; // 1 ~ 5
    private String reviewText;
    private Date reviewDate;

    public Review(String username, int rating, String reviewText) {
        this.username = username;
        this.rating = rating;
        this.reviewText = reviewText;
        this.reviewDate = new Date();
    }

    public String getUsername() { return username; }
    public int getRating() { return rating; }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return String.format("%s (Rating: %d) on %s: %s", username, rating, sdf.format(reviewDate), reviewText);
    }
}

// 도서 정보를 관리하는 클래스 (장르, 출판사, 리뷰 포함)
class Book implements Serializable {
    private static final long serialVersionUID = 1L;
    private String isbn;
    private String title;
    private String author;
    private double price;
    private int quantity;
    private String genre;
    private String publisher;
    private List<Review> reviews; // Review 클래스 사용

    public Book(String isbn, String title, String author, double price, int quantity, String genre, String publisher) {
        this.isbn     = isbn;
        this.title    = title;
        this.author   = author;
        this.price    = price;
        this.quantity = quantity;
        this.genre    = genre;
        this.publisher = publisher;
        this.reviews  = new ArrayList<>();
    }

    public String getIsbn()      { return isbn; }
    public String getTitle()     { return title; }
    public String getAuthor()    { return author; }
    public double getPrice()     { return price; }
    public int getQuantity()     { return quantity; }
    public String getGenre()     { return genre; }
    public String getPublisher() { return publisher; }

    public void setTitle(String title)       { this.title = title; }
    public void setAuthor(String author)     { this.author = author; }
    public void setPrice(double price)       { this.price = price; }
    public void setQuantity(int quantity)    { this.quantity = quantity; }
    public void setGenre(String genre)       { this.genre = genre; }
    public void setPublisher(String publisher){ this.publisher = publisher; }

    public void addReview(Review review) { reviews.add(review); }
    public List<Review> getReviews() { return reviews; }

    public double getAverageRating() {
        if (reviews.isEmpty()) return 0.0;
        int total = 0;
        for (Review r : reviews) {
            total += r.getRating();
        }
        return (double) total / reviews.size();
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | Author: %s | Price: $%.2f | Stock: %d | Genre: %s | Publisher: %s | Rating: %.2f (%d reviews)",
                isbn, title, author, price, quantity, genre, publisher, getAverageRating(), reviews.size());
    }
}

// 사용자 정보를 관리하는 클래스 (회원 등급과 포인트 추가)
class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private String username;
    private String password;
    UserRole role;
    private MembershipLevel membershipLevel;
    private int loyaltyPoints;

    public User(String username, String password, UserRole role) {
        this.username = username;
        this.password = password;
        this.role     = role;
        this.membershipLevel = MembershipLevel.NORMAL;
        this.loyaltyPoints = 0;
    }

    public String getUsername() { return username; }
    public UserRole getRole()   { return role; }
    public MembershipLevel getMembershipLevel() { return membershipLevel; }
    public int getLoyaltyPoints() { return loyaltyPoints; }

    public boolean checkPassword(String password) { return this.password.equals(password); }
    public void setPassword(String newPassword) { this.password = newPassword; }

    public void addLoyaltyPoints(int points) {
        loyaltyPoints += points;
        updateMembership();
    }

    // 간단한 기준에 따라 회원 등급 자동 업그레이드
    private void updateMembership() {
        if (loyaltyPoints >= 600) {
            membershipLevel = MembershipLevel.PLATINUM;
        } else if (loyaltyPoints >= 300) {
            membershipLevel = MembershipLevel.GOLD;
        } else if (loyaltyPoints >= 100) {
            membershipLevel = MembershipLevel.SILVER;
        } else {
            membershipLevel = MembershipLevel.NORMAL;
        }
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - Points: %d", username, membershipLevel, loyaltyPoints);
    }
}

// 주문 정보를 관리하는 클래스 (취소 및 반품 처리 기능 추가)
class Order implements Serializable {
    private static final long serialVersionUID = 1L;
    int orderId;  // 패키지 내부 접근용
    Book book;
    int orderQuantity;
    private String username;
    Date orderDate;
    private boolean cancelled;
    private boolean returned;

    public Order(int orderId, String username, Book book, int orderQuantity) {
        this.orderId = orderId;
        this.username = username;
        this.book = book;
        this.orderQuantity = orderQuantity;
        this.orderDate = new Date();
        this.cancelled = false;
        this.returned = false;
    }

    public double getTotalPrice() { return book.getPrice() * orderQuantity; }
    public String getUsername() { return username; }
    public boolean isCancelled() { return cancelled; }
    public boolean isReturned() { return returned; }
    public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
    public void setReturned(boolean returned) { this.returned = returned; }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String status = cancelled ? " (취소됨)" : (returned ? " (반품됨)" : "");
        return String.format("Order #%d: %s (Qty: %d) ordered by %s on %s | Total: $%.2f%s",
                orderId, book.getTitle(), orderQuantity, username, sdf.format(orderDate), getTotalPrice(), status);
    }
}

// 장바구니 관련 클래스
class CartItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private Book book;
    private int quantity;

    public CartItem(Book book, int quantity) {
        this.book = book;
        this.quantity = quantity;
    }

    public Book getBook() { return book; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    @Override
    public String toString() {
        return book.getTitle() + " (Qty: " + quantity + ")";
    }
}

class ShoppingCart implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<CartItem> items;

    public ShoppingCart() { items = new ArrayList<>(); }

    public void addItem(Book book, int quantity) {
        for (CartItem item : items) {
            if (item.getBook().getIsbn().equals(book.getIsbn())) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }
        items.add(new CartItem(book, quantity));
    }

    public boolean updateItem(String isbn, int quantity) {
        for (CartItem item : items) {
            if (item.getBook().getIsbn().equals(isbn)) {
                item.setQuantity(quantity);
                return true;
            }
        }
        return false;
    }

    public boolean removeItem(String isbn) {
        Iterator<CartItem> iter = items.iterator();
        while (iter.hasNext()) {
            CartItem item = iter.next();
            if (item.getBook().getIsbn().equals(isbn)) {
                iter.remove();
                return true;
            }
        }
        return false;
    }

    public void viewCart() {
        if (items.isEmpty()) {
            System.out.println("장바구니가 비어 있습니다.");
            return;
        }
        System.out.println("=== 장바구니 ===");
        for (CartItem item : items) {
            System.out.println(item);
        }
        System.out.printf("총 금액: $%.2f%n", getTotalPrice());
    }

    public double getTotalPrice() {
        double total = 0;
        for (CartItem item : items) {
            total += item.getBook().getPrice() * item.getQuantity();
        }
        return total;
    }

    public List<CartItem> getItems() { return items; }
    public void clear() { items.clear(); }
}

// PaymentGateway – 결제 및 환불 시뮬레이션
class PaymentGateway {
    public static boolean processPayment(double amount) {
        try {
            System.out.println("결제 처리 중... 금액: $" + amount);
            Thread.sleep(1000); // 지연 시뮬레이션
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("결제 완료.");
        return true;
    }

    public static boolean processRefund(double amount) {
        try {
            System.out.println("환불 처리 중... 금액: $" + amount);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("환불 완료.");
        return true;
    }
}

// RecommendationEngine – 사용자의 구매 내역과 선호 장르 기반 추천
class RecommendationEngine {
    public static List<Book> getRecommendations(BookMarket market, User user) {
        // 사용자가 이전에 주문한 도서의 장르를 수집
        Set<String> likedGenres = new HashSet<>();
        for (Order order : market.getOrders()) {
            if(order.getUsername().equals(user.getUsername()) && !order.isCancelled() && !order.isReturned()){
                likedGenres.add(order.book.getGenre());
            }
        }
        List<Book> recommendations = new ArrayList<>();
        for (Book book : market.getBooks().values()) {
            if (likedGenres.contains(book.getGenre()) && book.getQuantity() > 0) {
                recommendations.add(book);
            }
        }
        recommendations.sort((b1, b2) -> Double.compare(b2.getAverageRating(), b1.getAverageRating()));
        return recommendations;
    }
}

// NotificationManager – 알림 전송 시뮬레이션
class NotificationManager {
    public static void sendNotification(String username, String message) {
        System.out.println("알림 [To: " + username + "]: " + message);
    }
}

// BookMarket 클래스 – 비즈니스 로직 및 데이터 영속성, 추가 기능 다수 포함
class BookMarket implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(BookMarket.class.getName());

    private Map<String, Book> books;         // ISBN -> Book
    private Map<String, User> users;         // username -> User
    private List<Order> orders;              // 모든 주문 내역
    private int currentOrderIdCounter;
    private Map<String, ShoppingCart> carts; // username -> ShoppingCart
    private Map<String, List<String>> wishLists; // username -> [도서 ISBN 목록]

    public BookMarket() {
        books = new HashMap<>();
        users = new HashMap<>();
        orders = new ArrayList<>();
        currentOrderIdCounter = 1;
        carts = new HashMap<>();
        wishLists = new HashMap<>();
    }

    // Getter – RecommendationEngine 사용을 위함
    public Map<String, Book> getBooks() { return books; }
    public List<Order> getOrders() { return orders; }

    // 사용자 등록
    public boolean registerUser(String username, String password, UserRole role) {
        if (users.containsKey(username)) {
            System.out.println("이미 존재하는 사용자입니다.");
            return false;
        }
        users.put(username, new User(username, password, role));
        System.out.println("사용자 등록 완료: " + username);
        logger.log(Level.INFO, "신규 사용자 등록: {0}", username);
        return true;
    }

    // 로그인
    public User loginUser(String username, String password) {
        if (!users.containsKey(username)) {
            System.out.println("존재하지 않는 사용자입니다.");
            return null;
        }
        User user = users.get(username);
        if (user.checkPassword(password)) {
            logger.log(Level.INFO, "로그인 성공: {0}", username);
            return user;
        } else {
            System.out.println("비밀번호가 틀렸습니다.");
            return null;
        }
    }

    // 도서 관리
    public boolean addBook(Book book) {
        if (books.containsKey(book.getIsbn())) {
            System.out.println("이미 등록된 도서입니다.");
            return false;
        }
        books.put(book.getIsbn(), book);
        System.out.println("도서 추가됨: " + book);
        logger.log(Level.INFO, "도서 추가: {0}", book.getIsbn());
        return true;
    }

    public boolean updateBook(String isbn, String title, String author, double price, int quantity, String genre, String publisher) {
        if (!books.containsKey(isbn)) {
            System.out.println("해당 ISBN의 도서가 존재하지 않습니다.");
            return false;
        }
        Book book = books.get(isbn);
        book.setTitle(title);
        book.setAuthor(author);
        book.setPrice(price);
        book.setQuantity(quantity);
        book.setGenre(genre);
        book.setPublisher(publisher);
        System.out.println("도서 업데이트 완료: " + book);
        logger.log(Level.INFO, "도서 업데이트: {0}", isbn);
        return true;
    }

    public boolean removeBook(String isbn) {
        if (!books.containsKey(isbn)) {
            System.out.println("해당 ISBN의 도서가 존재하지 않습니다.");
            return false;
        }
        Book removed = books.remove(isbn);
        System.out.println("도서 제거됨: " + removed);
        logger.log(Level.INFO, "도서 제거: {0}", isbn);
        return true;
    }

    public void listBooks() {
        if (books.isEmpty()){
            System.out.println("등록된 도서가 없습니다.");
            return;
        }
        System.out.println("=== 도서 목록 ===");
        List<Book> bookList = new ArrayList<>(books.values());
        bookList.sort(Comparator.comparing(Book::getTitle));
        for(Book book : bookList) {
            System.out.println(book);
        }
    }

    public void searchBooks(String query) {
        query = query.toLowerCase();
        boolean found = false;
        System.out.println("=== 검색 결과 ===");
        for(Book book : books.values()) {
            if(book.getIsbn().toLowerCase().contains(query) ||
               book.getTitle().toLowerCase().contains(query) ||
               book.getGenre().toLowerCase().contains(query) ||
               book.getPublisher().toLowerCase().contains(query)) {
                System.out.println(book);
                found = true;
            }
        }
        if (!found) {
            System.out.println("검색 결과가 없습니다.");
        }
    }

    public Book getBook(String isbn) {
        return books.get(isbn);
    }

    // 주문 처리 (즉시 주문)
    public boolean placeOrder(String username, String isbn, int orderQuantity) {
        if (!books.containsKey(isbn)) {
            System.out.println("해당 ISBN의 도서가 존재하지 않습니다.");
            return false;
        }
        Book book = books.get(isbn);
        if (book.getQuantity() < orderQuantity) {
            System.out.println("재고가 부족합니다. 현재 재고: " + book.getQuantity());
            return false;
        }

        // 결제 처리
        if (!PaymentGateway.processPayment(book.getPrice() * orderQuantity)) {
            System.out.println("결제에 실패했습니다. 주문이 처리되지 않았습니다.");
            return false;
        }

        book.setQuantity(book.getQuantity() - orderQuantity);
        Order order = new Order(currentOrderIdCounter++, username, book, orderQuantity); // 수정된 부분
        orders.add(order);
        User user = users.get(username);
        if (user != null) {
            user.addLoyaltyPoints((int) (order.getTotalPrice() / 10)); // 10달러당 1포인트
            NotificationManager.sendNotification(username, String.format("주문이 완료되었습니다! 주문번호: %d", order.orderId));
        }
        System.out.println("주문 완료: " + order);
        logger.log(Level.INFO, "주문 생성: Order#{0} by {1} for {2}", new Object[]{order.orderId, username, book.getTitle()});
        return true;
    }

    // 주문 취소: 주문번호를 통해 주문 취소(재고 복원)
    public boolean cancelOrder(String username, int orderId) {
        for(Order order : orders) {
            if(order.orderId == orderId && order.getUsername().equals(username) && !order.isCancelled()){
                order.setCancelled(true);
                // 재고 복원
                order.book.setQuantity(order.book.getQuantity() + order.orderQuantity);
                NotificationManager.sendNotification(username, String.format("주문이 취소되었습니다! 주문번호: %d", order.orderId));
                System.out.println("주문이 취소되었습니다: " + order);
                logger.log(Level.INFO, "주문 취소: Order#{0} by {1}", new Object[]{order.orderId, username});
                return true;
            }
        }
        System.out.println("해당 주문을 찾지 못했거나 이미 취소되었습니다.");
        return false;
    }

    // 주문 반품: 사용자 요청에 의해 반품(재고 복원)
    public boolean returnOrder(String username, int orderId) {
        for(Order order : orders) {
            if(order.orderId == orderId && order.getUsername().equals(username) && !order.isReturned() && !order.isCancelled()){
                order.setReturned(true);
                order.book.setQuantity(order.book.getQuantity() + order.orderQuantity);
                PaymentGateway.processRefund(order.getTotalPrice()); // 환불 처리
                NotificationManager.sendNotification(username, String.format("주문이 반품되었습니다! 주문번호: %d", order.orderId));
                System.out.println("주문이 반품되었습니다: " + order);
                logger.log(Level.INFO, "주문 반품: Order#{0} by {1}", new Object[]{order.orderId, username});
                return true;
            }
        }
        System.out.println("반품 가능한 주문이 없습니다.");
        return false;
    }

    // 주문 내역 조회 (관리자 또는 사용자)
    public void viewOrders(String username) {
        boolean found = false;
        System.out.println("=== 주문 목록 ===");
        for(Order order : orders) {
            if(order.getUsername().equals(username)) {
                System.out.println(order);
                found = true;
            }
        }
        if (!found) {
            System.out.println("주문 내역이 없습니다.");
        }
    }

    // 위시리스트에 도서 추가
    public void addToWishlist(String username, String isbn) {
        if (!books.containsKey(isbn)) {
            System.out.println("해당 도서는 존재하지 않습니다.");
            return;
        }
        wishLists.putIfAbsent(username, new ArrayList<>());
        List<String> wishlist = wishLists.get(username);
        if (!wishlist.contains(isbn)) {
            wishlist.add(isbn);
            System.out.println("위시리스트에 추가되었습니다: " + books.get(isbn).getTitle());
            logger.log(Level.INFO, "위시리스트 추가: {0} by {1}", new Object[]{isbn, username});
        } else {
            System.out.println("이미 위시리스트에 존재하는 도서입니다.");
        }
    }

    // 위시리스트 조회
    public void viewWishlist(String username) {
        if (!wishLists.containsKey(username) || wishLists.get(username).isEmpty()) {
            System.out.println("위시리스트가 비어 있습니다.");
            return;
        }
        System.out.println("=== 위시리스트 ===");
        for (String isbn : wishLists.get(username)) {
            System.out.println(books.get(isbn));
        }
    }

    // 위시리스트에서 도서 제거
    public void removeFromWishlist(String username, String isbn) {
        if (!wishLists.containsKey(username) || !wishLists.get(username).contains(isbn)) {
            System.out.println("위시리스트에 해당 도서가 존재하지 않습니다.");
            return;
        }
        wishLists.get(username).remove(isbn);
        System.out.println("위시리스트에서 제거되었습니다: " + books.get(isbn).getTitle());
        logger.log(Level.INFO, "위시리스트 제거: {0} by {1}", new Object[]{isbn, username});
    }

    // 사용자의 구매 내역 및 선호 장르 기반 추천
    public List<Book> getRecommendedBooks(String username) {
        Set<String> likedGenres = new HashSet<>();
        for (Order order : orders) {
            if (order.getUsername().equals(username) && !order.isCancelled() && !order.isReturned()) {
                likedGenres.add(order.book.getGenre());
            }
        }

        List<Book> recommendations = new ArrayList<>();
        for (Book book : books.values()) {
            if (likedGenres.contains(book.getGenre()) && book.getQuantity() > 0) {
                recommendations.add(book);
            }
        }

        // 리뷰 평점을 기준으로 정렬 (높은 순)
        recommendations.sort((b1, b2) -> Double.compare(b2.getAverageRating(), b1.getAverageRating()));

        return recommendations;
    }

    // 추천 도서 출력
    public void showRecommendations(String username) {
        List<Book> recommendations = getRecommendedBooks(username);
        if (recommendations.isEmpty()) {
            System.out.println("추천할 도서가 없습니다.");
            return;
        }
        System.out.println("=== 추천 도서 목록 ===");
        for (Book book : recommendations) {
            System.out.println(book);
        }
    }

    // 전체 매출 보고서
    public void viewSalesReport() {
        if (orders.isEmpty()) {
            System.out.println("현재까지 판매 기록이 없습니다.");
            return;
        }

        double totalRevenue = 0;
        int totalOrders = orders.size();
        Map<String, Integer> bookSales = new HashMap<>();

        for (Order order : orders) {
            if (!order.isCancelled() && !order.isReturned()) {
                totalRevenue += order.getTotalPrice();
                String title = order.book.getTitle();
                bookSales.put(title, bookSales.getOrDefault(title, 0) + order.orderQuantity);
            }
        }

        System.out.println("=== 전체 매출 보고서 ===");
        System.out.println("총 주문 건수: " + totalOrders);
        System.out.printf("총 매출액: $%.2f%n", totalRevenue);
        System.out.println("=== 도서별 판매 현황 ===");
        // 도서별 판매 현황을 판매량 순으로 정렬
        bookSales.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> System.out.println(entry.getKey() + " : " + entry.getValue() + "권 판매"));
    }

    // 월별 매출 보고서
    public void viewMonthlySalesReport() {
        if (orders.isEmpty()) {
            System.out.println("현재까지 판매 기록이 없습니다.");
            return;
        }

        Map<String, Double> monthlyRevenue = new HashMap<>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        for (Order order : orders) {
            if (!order.isCancelled() && !order.isReturned()) {
                String month = sdf.format(order.orderDate);
                monthlyRevenue.put(month, monthlyRevenue.getOrDefault(month, 0.0) + order.getTotalPrice());
            }
        }

        System.out.println("=== 월별 매출 보고서 ===");
        // 월별 매출을 날짜 순으로 정렬
        monthlyRevenue.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> System.out.printf("%s : $%.2f%n", entry.getKey(), entry.getValue()));
    }

    // 베스트셀러 도서 목록
    public void viewBestSellers() {
        if (orders.isEmpty()) {
            System.out.println("판매된 도서가 없습니다.");
            return;
        }

        Map<String, Integer> bookSales = new HashMap<>();
        for (Order order : orders) {
            if (!order.isCancelled() && !order.isReturned()) {
                String isbn = order.book.getIsbn();
                bookSales.put(isbn, bookSales.getOrDefault(isbn, 0) + order.orderQuantity);
            }
        }

        List<Map.Entry<String, Integer>> sortedSales = new ArrayList<>(bookSales.entrySet());
        sortedSales.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        System.out.println("=== 베스트셀러 도서 (Top 5) ===");
        int count = Math.min(5, sortedSales.size());
        for (int i = 0; i < count; i++) {
            String isbn = sortedSales.get(i).getKey();
            Book book = books.get(isbn);
            int sold = sortedSales.get(i).getValue();
            System.out.println(String.format("%d위: %s - 판매량: %d권", i + 1, book.getTitle(), sold));
        }
    }

    // 매출 데이터를 CSV 파일로 저장
    public void exportSalesDataToCSV(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("Order ID,Username,Book Title,Quantity,Total Price,Date,Status");

            for (Order order : orders) {
                String status = order.isCancelled() ? "Cancelled" : order.isReturned() ? "Returned" : "Completed";
                writer.printf("%d,%s,%s,%d,%.2f,%s,%s%n",
                        order.orderId, order.getUsername(), order.book.getTitle(),
                        order.orderQuantity, order.getTotalPrice(),
                        new SimpleDateFormat("yyyy-MM-dd").format(order.orderDate), status);
            }

            System.out.println("매출 데이터가 CSV 파일로 저장되었습니다: " + filename);
            logger.log(Level.INFO, "매출 데이터 CSV 내보내기: {0}", filename);
        } catch (IOException e) {
            System.out.println("CSV 파일 저장 중 오류 발생: " + e.getMessage());
            logger.log(Level.SEVERE, "매출 데이터 CSV 내보내기 오류", e);
        }
    }

    // 사용자 데이터를 CSV 파일로 저장
    public void exportUserDataToCSV(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("Username,Role,Membership Level,Loyalty Points");

            for (User user : users.values()) {
                writer.printf("%s,%s,%s,%d%n",
                        user.getUsername(), user.getRole(), user.getMembershipLevel(), user.getLoyaltyPoints());
            }

            System.out.println("사용자 데이터가 CSV 파일로 저장되었습니다: " + filename);
            logger.log(Level.INFO, "사용자 데이터 CSV 내보내기: {0}", filename);
        } catch (IOException e) {
            System.out.println("CSV 파일 저장 중 오류 발생: " + e.getMessage());
            logger.log(Level.SEVERE, "사용자 데이터 CSV 내보내기 오류", e);
        }
    }

    // 특정 임계값 이하인 도서 목록 출력 (재고 부족 예상)
    public void viewLowStockBooks(int threshold) {
        System.out.println("=== 재고 부족 예상 도서 목록 (임계값: " + threshold + ") ===");
        boolean found = false;
        for (Book book : books.values()) {
            if (book.getQuantity() <= threshold) {
                System.out.println(book);
                found = true;
            }
        }
        if (!found) {
            System.out.println("재고 부족이 예상되는 도서가 없습니다.");
        }
    }

    // 빠르게 소진되는 도서 목록 (최근 많이 주문된 도서)
    public void viewFastSellingBooks(int orderThreshold) {
        Map<String, Integer> bookSales = new HashMap<>();

        for (Order order : orders) {
            if (!order.isCancelled() && !order.isReturned()) {
                String isbn = order.book.getIsbn();
                bookSales.put(isbn, bookSales.getOrDefault(isbn, 0) + order.orderQuantity);
            }
        }

        List<Map.Entry<String, Integer>> sortedSales = new ArrayList<>(bookSales.entrySet());
        sortedSales.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        System.out.println("=== 빠르게 소진되는 도서 목록 (최근 주문량 기준: " + orderThreshold + ") ===");
        boolean found = false;
        for (Map.Entry<String, Integer> entry : sortedSales) {
            if (entry.getValue() >= orderThreshold) {
                System.out.println(books.get(entry.getKey()) + " - 판매량: " + entry.getValue());
                found = true;
            }
        }
        if (!found) {
            System.out.println("빠르게 소진되는 도서가 없습니다.");
        }
    }

    // 시스템 기본 상태 점검
    public void checkSystemHealth() {
        System.out.println("=== 시스템 건강 상태 점검 ===");

        // 데이터 저장 검사
        File dataFile = new File("bookmarket.dat");
        if (dataFile.exists()) {
            System.out.println("✅ 데이터 저장 상태: 정상 (" + dataFile.length() + " 바이트)");
        } else {
            System.out.println("❌ 데이터 저장 상태: 오류 (파일 없음)");
        }

        // 사용자 수 검사
        System.out.println("✅ 등록된 사용자 수: " + users.size());

        // 도서 수 검사
        System.out.println("✅ 등록된 도서 수: " + books.size());

        // 주문 수 검사
        int activeOrders = (int) orders.stream().filter(o -> !o.isCancelled() && !o.isReturned()).count();
        System.out.println("✅ 활성 주문 수: " + activeOrders);

        // 장바구니 데이터 검사
        System.out.println("✅ 사용자 장바구니 수: " + carts.size());

        // 위시리스트 데이터 검사
        System.out.println("✅ 위시리스트 등록된 사용자 수: " + wishLists.size());

        // 시스템 전체 검사 완료
        System.out.println("🚀 시스템 상태 확인 완료!");
    }

    // 사용자 권한 변경 (관리자 전용)
    public boolean changeUserRole(String adminUsername, String targetUsername, UserRole newRole) {
        if (!users.containsKey(adminUsername) || users.get(adminUsername).getRole() != UserRole.ADMIN) {
            System.out.println("권한 변경은 관리자만 수행할 수 있습니다.");
            return false;
        }
        if (!users.containsKey(targetUsername)) {
            System.out.println("해당 사용자 계정이 존재하지 않습니다.");
            return false;
        }
        users.get(targetUsername).role = newRole;
        System.out.println("사용자 권한이 변경되었습니다: " + targetUsername + " -> " + newRole);
        logger.log(Level.INFO, "사용자 권한 변경: {0} -> {1}", new Object[]{targetUsername, newRole});
        return true;
    }

    // 사용자 목록 조회 (관리자 전용)
    public void listUsers() {
        System.out.println("=== 사용자 목록 ===");
        for (User user : users.values()) {
            System.out.println(user);
        }
    }

    // 시스템 로그 조회 (관리자 전용)
    public void viewSystemLogs() {
        try {
            File logFile = new File("system_logs.txt");
            if (!logFile.exists()) {
                System.out.println("로그 파일이 존재하지 않습니다.");
                return;
            }

            System.out.println("=== 시스템 로그 조회 ===");
            try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }
        } catch (IOException e) {
            System.out.println("로그 파일 읽기 중 오류 발생: " + e.getMessage());
        }
    }

    // 로그 추가 (내부 사용)
    public void addLogEntry(String entry) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("system_logs.txt", true))) {
            writer.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " - " + entry);
        } catch (IOException e) {
            System.out.println("로그 파일 저장 중 오류 발생: " + e.getMessage());
        }
    }
}


public class BookMarketApp {
    public static void main(String[] args) {
        BookMarket market = new BookMarket();
        Scanner sc = new Scanner(System.in);
        User loggedInUser = null;

        // 샘플 데이터 등록
        market.addBook(new Book("111", "자바의 정석", "남궁성", 33000, 10, "프로그래밍", "도우출판"));
        market.addBook(new Book("222", "이펙티브 자바", "조슈아 블로크", 38000, 5, "프로그래밍", "인사이트"));
        market.registerUser("admin", "admin", UserRole.ADMIN);
        market.registerUser("user1", "1111", UserRole.CUSTOMER);

        while (true) {
            if (loggedInUser == null) {
                showGuestMenu();
                String sel = sc.nextLine();
                if ("1".equals(sel)) {
                    guestRegister(market, sc);
                } else if ("2".equals(sel)) {
                    loggedInUser = guestLogin(market, sc);
                } else if ("3".equals(sel)) {
                    market.listBooks();
                } else if ("0".equals(sel)) {
                    System.out.println("프로그램 종료");
                    break;
                }
            } else {
                if (loggedInUser.getRole() == UserRole.ADMIN) {
                    showAdminMenu();
                    String sel = sc.nextLine();
                    if ("1".equals(sel)) {
                        market.listBooks();
                    } else if ("2".equals(sel)) {
                        adminAddBook(market, sc);
                    } else if ("3".equals(sel)) {
                        adminRemoveBook(market, sc);
                    } else if ("4".equals(sel)) {
                        adminSalesReport(market);
                    } else if ("5".equals(sel)) {
                        market.checkSystemHealth();
                    } else if ("6".equals(sel)) {
                        loggedInUser = null;
                        System.out.println("로그아웃 되었습니다.");
                    }
                } else {
                    showUserMenu();
                    String sel = sc.nextLine();
                    if ("1".equals(sel)) {
                        market.listBooks();
                    } else if ("2".equals(sel)) {
                        userOrderBook(market, sc, loggedInUser);
                    } else if ("3".equals(sel)) {
                        userViewMyInfo(loggedInUser);
                    } else if ("4".equals(sel)) {
                        userViewMyOrders(market, loggedInUser);
                    } else if ("5".equals(sel)) {
                        userAddReview(market, sc, loggedInUser);
                    } else if ("6".equals(sel)) {
                        loggedInUser = null;
                        System.out.println("로그아웃 되었습니다.");
                    }
                }
            }
        }
        sc.close();
    }

    // ------ 메뉴 표시 ------
    static void showGuestMenu() {
        System.out.println("\n=== Book Market(비회원) ===");
        System.out.println("1. 회원가입");
        System.out.println("2. 로그인");
        System.out.println("3. 도서목록보기");
        System.out.println("0. 종료");
        System.out.print("선택> ");
    }

    static void showAdminMenu() {
        System.out.println("\n=== Book Market(관리자) ===");
        System.out.println("1. 도서목록보기");
        System.out.println("2. 도서등록");
        System.out.println("3. 도서삭제");
        System.out.println("4. 매출보고서");
        System.out.println("5. 시스템 건강 점검");
        System.out.println("6. 로그아웃");
        System.out.print("선택> ");
    }

    static void showUserMenu() {
        System.out.println("\n=== Book Market(회원) ===");
        System.out.println("1. 도서목록보기");
        System.out.println("2. 도서주문");
        System.out.println("3. 내정보보기");
        System.out.println("4. 내 주문내역");
        System.out.println("5. 도서리뷰작성");
        System.out.println("6. 로그아웃");
        System.out.print("선택> ");
    }

    // ------ 비회원 기능 ------
    static void guestRegister(BookMarket market, Scanner sc) {
        System.out.print("아이디: ");
        String id = sc.nextLine();
        System.out.print("비밀번호: ");
        String pw = sc.nextLine();
        market.registerUser(id, pw, UserRole.CUSTOMER);
    }

    static User guestLogin(BookMarket market, Scanner sc) {
        System.out.print("아이디: ");
        String id = sc.nextLine();
        System.out.print("비밀번호: ");
        String pw = sc.nextLine();
        return market.loginUser(id, pw);
    }

    // ------ 관리자 기능 ------
    static void adminAddBook(BookMarket market, Scanner sc) {
        System.out.print("ISBN: ");
        String isbn = sc.nextLine();
        System.out.print("제목: ");
        String title = sc.nextLine();
        System.out.print("저자: ");
        String author = sc.nextLine();
        System.out.print("가격: ");
        double price = Double.parseDouble(sc.nextLine());
        System.out.print("수량: ");
        int qty = Integer.parseInt(sc.nextLine());
        System.out.print("장르: ");
        String genre = sc.nextLine();
        System.out.print("출판사: ");
        String pub = sc.nextLine();
        market.addBook(new Book(isbn, title, author, price, qty, genre, pub));
    }

    static void adminRemoveBook(BookMarket market, Scanner sc) {
        System.out.print("삭제할 도서 ISBN: ");
        String isbn = sc.nextLine();
        market.removeBook(isbn);
    }

    static void adminSalesReport(BookMarket market) {
        market.viewSalesReport();
        market.viewMonthlySalesReport();
        market.viewBestSellers();
    }

    // ------ 회원 기능 ------
    static void userOrderBook(BookMarket market, Scanner sc, User user) {
        market.listBooks();
        System.out.print("주문할 도서 ISBN: ");
        String isbn = sc.nextLine();
        System.out.print("수량: ");
        int qty = Integer.parseInt(sc.nextLine());
        market.placeOrder(user.getUsername(), isbn, qty);
    }

    static void userViewMyInfo(User user) {
        System.out.println("내 정보: " + user);
    }

    static void userViewMyOrders(BookMarket market, User user) {
        market.viewOrders(user.getUsername());
    }

    static void userAddReview(BookMarket market, Scanner sc, User user) {
        market.listBooks();
        System.out.print("리뷰를 작성할 도서 ISBN: ");
        String isbn = sc.nextLine();
        Book book = market.getBook(isbn);
        if (book == null) {
            System.out.println("해당 ISBN의 도서가 없습니다.");
            return;
        }
        System.out.print("별점(1~5): ");
        int rating = Integer.parseInt(sc.nextLine());
        System.out.print("리뷰 내용: ");
        String reviewText = sc.nextLine();
        book.addReview(new Review(user.getUsername(), rating, reviewText));
        System.out.println("리뷰가 등록되었습니다.");
    }
}