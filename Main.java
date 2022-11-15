package carsharing;

import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    static Connection conn = null;
    static Statement st = null;
    static String DB_URL = "jdbc:h2:./src/carsharing/db/";

    static ArrayList<String> companyList = new ArrayList<>();
    static int pickedCompany = 0;
    static ArrayList<String> customerList = new ArrayList<>();
    static int pickedCustomer = 0;
    static ArrayList<Integer> rentedCars = new ArrayList<>();

    public static void main(String[] args) throws SQLException {

        getDbFileName(args);

        try{
            Class.forName ("org.h2.Driver");
            conn = DriverManager.getConnection (DB_URL,"","");
            st = conn.createStatement();
        }catch(SQLException | RuntimeException | ClassNotFoundException e) {
            System.out.println(e);
        }

        createTables();
        loginMenu();

    }


    public static void getDbFileName(String[] inputArguments) {
        if (inputArguments == null) {
            return;
        }
        if (inputArguments.length == 2 && "-databaseFileName".equals(inputArguments[0])) {
            DB_URL += inputArguments[1];
        } else {
            DB_URL += "carsharing";
        }
    }


    public static void createTables(){

        String company = "CREATE TABLE IF NOT EXISTS company (" +
                "id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR NOT NULL UNIQUE)";

        String car = "create table if not exists car " +
                "(id int auto_increment primary key, name varchar not null unique, company_id int not null, " +
                "foreign key (company_id) references company (id));";


        String customer = "CREATE TABLE IF NOT EXISTS customer(" +
                "id INT PRIMARY KEY AUTO_INCREMENT, " +
                "name VARCHAR NOT NULL UNIQUE, " +
                "rented_car_id INT DEFAULT NULL, " +
                "foreign key (rented_car_id) references car (id));";

        try {
            st.executeUpdate(company);
            st.executeUpdate(car);
            st.executeUpdate(customer);
        }catch(SQLException e){
            System.out.println(e);
        }
    }


    public static int decision() {

        Scanner input  = new Scanner(System.in);

        return input.nextInt();
    }


    public static void loginMenu() throws SQLException {

        do {
            System.out.println("1. Log in as a manager\n" +
                    "2. Log in as a customer\n" +
                    "3. Create a customer\n" +
                    "0. Exit");

            switch (decision()) {
                case 1: do {
                             mainMenuManager();
                        }while (mainMenuDecision());
                    break;
                case 2: boolean isEmpty = showCustomer(); if(!isEmpty){ pickedCustomer = decision()-1; customerCarMenu();break;}break;
                case 3: createCustomer();break;
                default:
                    return;
            }
        }while (true);
    }


    public static void mainMenuManager() {

        System.out.println(
                        "1. Company list\n" +
                        "2. Create a company\n" +
                        "0. Back"
        );
    }


    public static boolean mainMenuDecision() throws SQLException {

        switch (decision()) {
            case 1:
                if (showCompanyList()) {
                    pickedCompany = decision() - 1;
                    if (pickedCompany == -1) {
                        break;
                    }
                    carMenuManager();
                }
                break;
            case 2:
                createCompany();
                break;
            case 0: return false;
        }
        return true;
    }


    // Der Inhalt der "companyList" wird gelöscht und mit den aktuellen Daten befüllt
    // Array wird ausgegeben

    public static boolean showCompanyList () throws SQLException {

        ResultSet rs = null;

        companyList.clear();

        try {
            String sql = "select * from company";
            rs = st.executeQuery(sql);
        }catch(SQLException e){
            System.out.println(e);
        }

        assert rs != null;
        if (!rs.next()) {
            System.out.println("The company list is empty");
            return false;

        }else {
            int ctr = 1;
            System.out.println("Choose the company:");

            do {
                System.out.println(ctr + ". " + rs.getString("name"));
                ctr++;
                companyList.add(rs.getString("name"));

            }while(rs.next());
            System.out.println("0. Back");
        }

        return true;
    }


    public static void carMenuManager() throws SQLException {

        do {
            System.out.println("'" + companyList.get(pickedCompany) + "'\n" +
                    "1. Car list\n" +
                    "2. Create a car\n" +
                    "0. Back");

            switch (decision()) {
                case 1: showCarList(); break;
                case 2: createCar(); break;
                case 0: return;
            }

        }while (true);
    }
    public static void showCarList() throws SQLException {

        ResultSet rs = null;
        int ctr = 1;
        int companyId = 0;
        String sql = "select * from company where name='" + companyList.get(pickedCompany) + "'";

        try{
            rs = st.executeQuery(sql);
        }catch (SQLException e) {
            System.out.println(e);
        }

        while (true){
            assert rs != null;
            if (!rs.next()) break;
            companyId = rs.getInt("id");
        }

        sql = "select * from car where(company_id = '" + companyId + "')";

        try{
            rs = st.executeQuery(sql);
        }catch (SQLException e) {
            System.out.println(e);
        }

        if (!rs.next()) {
            System.out.println("The car list is empty!");

        } else {
            do{
                System.out.println(ctr + ". " + rs.getString("name"));
                ctr++;
            }while(rs.next());
        }
    }


    public static void createCar() throws SQLException {

        System.out.println("Enter the car name:");

        ResultSet rs = null;
        Scanner input = new Scanner(System.in);
        String carName = input.nextLine();
        int companyID = 0;

        String sql = "select id from company where name='" + companyList.get(pickedCompany) + "'";

        try{
            rs = st.executeQuery(sql);
        }catch (SQLException e) {
            System.out.println(e);
        }

        while(true){
            assert rs != null;
            if (!rs.next()) break;
            companyID = rs.getInt("id");
        }

        sql = "insert into car (name,company_id) values ('" + carName + "', '" + companyID + "')";

        try{
            st.executeUpdate(sql);
        }catch (SQLException e) {
            System.out.println(e);
        }
    }


    public static void createCompany() {

        Scanner input = new Scanner(System.in);

        System.out.println("Enter the company name:");

        String newCompany = input.nextLine();
        String sql = "INSERT INTO company (NAME) VALUES ('" + newCompany +"');";

        try {
            st.executeUpdate(sql);
        }catch(SQLException e){
            System.out.println(e);
        }
    }


    public static void createCustomer() {

        System.out.println("Enter the customer name:");

        Scanner input = new Scanner(System.in);

        String customerName = input.nextLine();

        String sql = "insert into customer (name) values ('" + customerName + "')";

        try {
            st.executeUpdate(sql);
        }catch(SQLException e){
            System.out.println(e);
        }

        System.out.println("The customer was added!");
    }


    public static boolean showCustomer() throws SQLException {

        ResultSet rs = null;
        String sql = "select * from customer";

        try{
            rs = st.executeQuery(sql);
        }catch (SQLException e) {
            System.out.println(e);
        }

        assert rs != null;
        if (!rs.next()) {
            System.out.println("The customer list is empty!");
            return true;
        } else {
            int ctr = 1;
            customerList.clear();
            System.out.println("Customer list:");

            do{
                customerList.add(rs.getString("name"));
                System.out.println(ctr + ". " + rs.getString("name"));
                ctr++;

            }while(rs.next());
        }
        return false;
    }


    public static void customerCarMenu() throws SQLException {

        while(true){
            System.out.println("1. Rent a car\n" +
                    "2. Return a rented car\n" +
                    "3. My rented car\n" +
                    "0. Back");

            switch (decision()) {
                case 1: rentCar();break;
                case 2: returnRentedCar();break;
                case 3: showRentedCar();break;
                case 0: return;
            }
        }
    }


    public static void rentCar() throws SQLException {

        ResultSet rs = null;

        int ctr = 1;
        boolean hasRented = false;
        ArrayList<Integer> companyListID = new ArrayList<>();
        ArrayList<Integer> carListID = new ArrayList<>();

        String sql = "select * from customer where name='" + customerList.get(pickedCustomer) + "'";

        try{
            rs = st.executeQuery(sql);
        }catch (SQLException e) {
            System.out.println(e);
        }

        while(true){
            assert rs != null;
            if (!rs.next()) break;
            if (rs.getInt("rented_car_id") > 0){
                hasRented = true;
            }
        }

        if (hasRented) {
            System.out.println("You've already rented a car!");
            return;
        }

        sql = "select * from company";

        try{
            rs = st.executeQuery(sql);
        }catch (SQLException e) {
            System.out.println(e);
        }

        System.out.println("Choose a company:");

        while (rs.next()){
            companyListID.add(rs.getInt("id"));
            System.out.println(ctr + ". " + rs.getString("name"));
            ctr++;
        }

        System.out.println("0. Back");

        int companyID = companyListID.get(decision()-1);

        if (companyID == -1){return;}

        checkIfCarIsRented();

        sql = "select * from car where company_id ='" + companyID + "'";

        try{
            rs = st.executeQuery(sql);
        }catch (SQLException e) {
            System.out.println(e);
        }

        ctr = 1;

        System.out.println("Choose a car:");

        while(rs.next()){
            int counter = 0;
            for (Integer rentedCar : rentedCars) {
                if (rs.getInt("id") == rentedCar) {
                    counter++;
                }
            }

            if (counter == 0) {
                carListID.add(rs.getInt("id"));
                System.out.println(ctr + ". " + rs.getString("name"));
                ctr++;
            }
        }
        System.out.println("0. Back");

        int decision = decision()-1;

        if (decision == -1){return;}

        int carId = carListID.get(decision);

        sql = "update customer set rented_car_id = '" + carId + "'" + "where name = '" + customerList.get(pickedCustomer) + "'";

        try {
            st.executeUpdate(sql);
        }catch(SQLException e){
            System.out.println(e);
        }

        sql = "select * from car where id = '" + carId + "'";

        try{
            rs = st.executeQuery(sql);
        }catch (SQLException e) {
            System.out.println(e);
        }

        while(rs.next()){
            System.out.println("You rented '" + rs.getString("name") + "'");
        }

    }


    public static void returnRentedCar() throws SQLException {

        ResultSet rs = null;
        String sql = "select * from customer where name = '" + customerList.get(pickedCustomer) + "'";

        try{
            rs = st.executeQuery(sql);
        }catch (SQLException e) {
            System.out.println(e);
        }

        while(true){
            assert rs != null;
            if (!rs.next()) break;
            if (rs.getInt("rented_car_id") > 0){

                sql = "update customer set rented_car_id = null where name = '" + customerList.get(pickedCustomer) + "'";

                try {
                    st.executeUpdate(sql);
                }catch(SQLException e){
                    System.out.println(e);
                }

                System.out.println("You've returned a rented car!");

                return;

            } else {
                System.out.println("You didn't rent a car!");
            }
        }
    }


    public static void checkIfCarIsRented() throws SQLException {

        ResultSet rs = null;
        String sql = "select * from customer";
        rentedCars.clear();

        try{
            rs = st.executeQuery(sql);
        }catch (SQLException e) {
            System.out.println(e);
        }

        while(true){
            assert rs != null;
            if (!rs.next()) break;
            if(rs.getInt("rented_car_id")>0){
                rentedCars.add(rs.getInt("rented_car_id"));
            }
        }
    }


    public static void showRentedCar() throws SQLException {

        ResultSet rs;
        String sql = "select * from customer where name = '" + customerList.get(pickedCustomer) + "'";

        try{
            rs = st.executeQuery(sql);
            while(true) {
                assert rs != null;
                if (!rs.next()) break;

                int rented_car_id = rs.getInt("rented_car_id");

                if (rented_car_id > 0) {
                    sql = "select * from car where id = '" + rs.getInt("rented_car_id") + "'";
                    
                    rs = st.executeQuery(sql);

                    while(rs.next()){
                        System.out.println("Your rented car:\n" +
                                rs.getString("name"));

                        sql = "select * from company where id='" + rs.getInt("company_id") + "'";

                        rs = st.executeQuery(sql);

                        while(rs.next()) {
                            System.out.println("Company:\n" +
                                    rs.getString("name"));
                        }
                    }
                } else {
                    System.out.println("You didn't rent a car!");
                }
            }
        }catch (SQLException e) {
            System.out.println(e);
        }
    }
}