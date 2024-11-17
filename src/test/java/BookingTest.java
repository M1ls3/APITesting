import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class BookingTest {

    private static String tkn;

    @BeforeClass
    public static void setup() {
        RestAssured.baseURI = "https://restful-booker.herokuapp.com";
    }

    @BeforeClass
    public static void tokenInit() {
        Response res = given()
                .contentType(ContentType.JSON)
                .body("{\"username\": \"admin\", \"password\": \"password123\"}")
                .when()
                .post("https://restful-booker.herokuapp.com/auth");

        tkn = res.jsonPath().getString("token");
    }

    @Test
    public void authReturnsTkn() {
        Response res = given()
                .contentType(ContentType.JSON)
                .body("{\"username\": \"admin\", \"password\": \"password123\"}")
                .when()
                .post("/auth");

        res.then().statusCode(200)
                .body("token", notNullValue());
    }

    @Test
    public void getBookingsOk() {
        given().log().all()
                .when().get("/booking")
                .then().statusCode(200);
    }

    @Test
    public void bookingIdNotFound() {
        given().log().all()
                .when().get("/booking/100000")
                .then().statusCode(404);
    }

    @Test
    public void bookingIdHasBody() {
        given().log().all()
                .when().get("/booking/13")
                .then()
                .statusCode(200)
                .body("firstname", notNullValue())
                .body("lastname", notNullValue());
    }

    @Test
    public void getBookingByIdOk() {
        given().log().all()
                .when().get("/booking/50")
                .then().statusCode(200);
    }

    @Test
    public void getBookingWithFiltersOk() {
        Response res = given().log().all()
                .queryParam("firstname", "John")
                .queryParam("lastname", "Doe")
                .when()
                .get("/booking");

        res.then().statusCode(200)
                .body(not(empty()));
    }

    @Test
    public void updateBookingOk() {
        String reqBody = "{\n" +
                "    \"firstname\" : \"John\",\n" +
                "    \"lastname\" : \"Doe\",\n" +
                "    \"totalprice\" : 123,\n" +
                "    \"depositpaid\" : false,\n" +
                "    \"bookingdates\" : {\n" +
                "        \"checkin\" : \"2018-01-01\",\n" +
                "        \"checkout\" : \"2019-01-01\"\n" +
                "    },\n" +
                "    \"additionalneeds\" : \"Breakfast\"\n" +
                "}";

        Response res = given()
                .cookie("token", tkn)
                .contentType(ContentType.JSON)
                .body(reqBody)
                .log().all()
                .when()
                .put("/booking/14");

        res.then().log().all()
                .statusCode(200)
                .body("firstname", equalTo("John"))
                .body("lastname", equalTo("Doe"))
                .body("totalprice", equalTo(123))
                .body("depositpaid", equalTo(false));
    }

    @Test
    public void partialUpdateOk() {
        String reqBody = "{\n" +
                "    \"firstname\" : \"John\",\n" +
                "    \"lastname\" : \"Doe\",\n" +
                "    \"totalprice\" : 123,\n" +
                "    \"depositpaid\" : false\n" +
                "}";

        Response res = given()
                .cookie("token", tkn)
                .contentType(ContentType.JSON)
                .body(reqBody)
                .log().all()
                .when()
                .patch("/booking/14");

        res.then().log().all()
                .statusCode(200)
                .body("firstname", equalTo("John"))
                .body("lastname", equalTo("Doe"))
                .body("totalprice", equalTo(123))
                .body("depositpaid", equalTo(false));
    }

    @Test
    public void createBookingOk() {
        String reqBody = "  {\n" +
                "  \"firstname\" : \"Jim\",\n" +
                "    \"lastname\" : \"Brown\",\n" +
                "    \"totalprice\" : 111,\n" +
                "    \"depositpaid\" : true,\n" +
                "    \"bookingdates\" : {\n" +
                "        \"checkin\" : \"2018-01-01\",\n" +
                "        \"checkout\" : \"2019-01-01\"\n" +
                "    },\n" +
                "    \"additionalneeds\" : \"Breakfast\"\n" +
                "  }";

        Response res = given()
                .cookie("token", tkn)
                .contentType(ContentType.JSON)
                .body(reqBody)
                .when()
                .post("/booking");

        res.then().statusCode(200)
                .body("booking.firstname", equalTo("Jim"))
                .body("booking.lastname", equalTo("Brown"))
                .body("booking.totalprice", equalTo(111))
                .body("booking.depositpaid", equalTo(true));
    }

    @Test
    public void deleteBookingOk() {
        given().log().all()
                .cookie("token", tkn)
                .when().delete("/booking/26")
                .then().statusCode(anyOf(is(201), is(405)));
    }

    @Test
    public void deleteBookingForbidden() {
        given().log().all()
                .cookie("token", "no token :(")
                .when().delete("/booking/26")
                .then().statusCode(403);
    }
}
