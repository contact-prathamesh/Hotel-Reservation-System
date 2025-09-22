import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.Scanner;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.PreparedStatement;


public class HotelReservationSystem {

    private static final String url = "jdbc:mysql://localhost:3306/hotel_db";
    private static final String username = "root";
    private static final String password = "Pratham";

    public static void main ( String[] args ) {

        try {
            Class.forName ( "com.mysql.cj.jdbc.Driver" );
        } catch (ClassNotFoundException e) {
            System.out.println ( e.getMessage ( ) );
        }

        try {
            //- Created a .getConnection method to get an arguments like - url, username, password to establish connection between database
            Connection connection = DriverManager.getConnection ( url, username, password );
            while ( true ) {
                System.out.println ( );
                System.out.println ( "HOTEL MANAGEMENT SYSTEM" );
                Scanner scanner = new Scanner ( System.in );
                System.out.println ( "1. Reserve a room " );
                System.out.println ( "2. View reservation" );
                System.out.println ( "3. Get a Room number" );
                System.out.println ( "4. Update Reservation" );
                System.out.println ( "5. Delete Reservation" );
                System.out.println ( "0. Exit" );
                System.out.println ( "Choose an option" );

                //- Option will be get stored in choice(Variable) with the help of Scanner class
                int choice = scanner.nextInt ( );

                // Created a switch case to invoke a function to do according to users choice with passing an arguments called connection and scanner -
                switch (choice) {
                    case 1:
                        reserveRoom ( connection, scanner );
                        break;
                    case 2:
                        viewReservations ( connection );
                        break;
                    case 3:
                        getRoomNumber ( connection, scanner );
                        break;
                    case 4:
                        updateReservation ( connection, scanner );
                        break;
                    case 5:
                        deleteReservation ( connection, scanner );
                        break;
                    case 0:

                        exit ( );
                        scanner.close ( );
                        return;
                    default:
                        System.out.println ( "Invalid choice. Try again" );
                }
            }
        } catch (SQLException e) {
            System.out.println ( e.getMessage ( ) );
        } catch (
                InterruptedException e) {                    // Created catch exception to solve error from exit() method
            throw new RuntimeException ( );                     // Used throw to so we can throw a simple exception explicitaly with the help of throw new
        }
    }

    private static void reserveRoom(Connection connection, Scanner scanner) {
        try {
            System.out.println("Enter guest name : ");
            String guestName = scanner.next();
            scanner.nextLine();
            System.out.println("Enter room number : ");
            int roomNumber = scanner.nextInt();
            System.out.println("Enter contact number");
            String contactNumber = scanner.next();

            // The SQL query is changed to use '?' as placeholders for security.
            String sql = "INSERT INTO reservations (guest_name, room_number, contact_number) VALUES (?, ?, ?)";

//- Created a statement interface to run sql queries in java languages, used method called executeUpdate to run "sql" query;
            // The 'Statement' is replaced with a 'PreparedStatement' for security.
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                // The user's input is safely set as parameters.
                preparedStatement.setString(1, guestName);
                preparedStatement.setInt(2, roomNumber);
                preparedStatement.setString(3, contactNumber);

// executeUpdate() is used for SQL commands that modify data (INSERT, UPDATE, DELETE) and returns the number of affected rows.
                int affectRows = preparedStatement.executeUpdate();
                if (affectRows > 0) {
                    System.out.println("Reservation successful.");
                } else {
                    System.out.println("Reservation failed.");
                }
            }
// When using the Statement interface, an SQLException might occur, which is what we resolve here.
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void viewReservations ( Connection connection ) throws SQLException {

        String sql = "SELECT reservation_id, guest_name, room_number, contact_number, reservation_date FROM reservations";

// Created one Interface with "ResultSet" method which help us to create one instance(resultSet) which store valur of sql query;
        try (Statement statement = connection.createStatement ( );
             ResultSet resultSet = statement.executeQuery ( sql )) {


            System.out.println ( "Current Reservation: " );
            System.out.println ( "+----------------+-----------------+---------------+----------------------+-------------------------+" );
            System.out.println ( "| Reservation ID | Guest           | Room Number   | Contact Number      | Reservation Date        |" );
            System.out.println ( "+----------------+-----------------+---------------+----------------------+-------------------------+" );

            while ( resultSet.next ( ) ) {
                int reservationId = resultSet.getInt ( "reservation_id" );
                String guestName = resultSet.getString ( "guest_name" );
                int roomNumber = resultSet.getInt ( "room_number" );
                String contactNumber = resultSet.getString ( "contact_number" );
                String reservationDate = resultSet.getTimestamp ( "reservation_date" ).toString ( );

                // Format and display the reservation data in a table-like format
                System.out.printf ( "| %-14d | %-15s | %-13d | %-20s | %-19s   |\n",
                        reservationId, guestName, roomNumber, contactNumber, reservationDate );
            }
            System.out.println ( "+----------------+-----------------+---------------+----------------------+-------------------------+" );
        }
    }

    private static void getRoomNumber ( Connection connection, Scanner scanner ) {

        try {
            System.out.println("Enter reservation ID : ");
            int reservationID = scanner.nextInt();
            System.out.println("Enter guest name :");
            String guestName = scanner.next();

            String sql = "SELECT room_number FROM reservations " +
                    "WHERE reservation_id = " + reservationID +
                    " AND guest_name = '" + guestName + "'";

            try (Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql)) {

                if (resultSet.next()) {
                    int roomNumber = resultSet.getInt("room_number");
                    System.out.println("Success! The room number is: " + roomNumber);
                } else {
                    System.out.println("Reservation not found for the given ID and guest name.");
                }
            }
        } catch(SQLException e){
            e.printStackTrace();
        }
    }

    private static void updateReservation ( Connection connection, Scanner scanner ) {

        try {
            System.out.print("Enter reservation ID to update: ");
            int reservationId = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            if (!reservationExists(connection, reservationId)) {
                System.out.println("Reservation not found for the given ID.");
                return;
            }

            System.out.print("Enter new guest name: ");
            String newGuestName = scanner.nextLine();
            System.out.print("Enter new room number: ");
            int newRoomNumber = scanner.nextInt();
            System.out.print("Enter new contact number: ");
            String newContactNumber = scanner.next();

            String sql = "UPDATE reservations SET guest_name = '" + newGuestName + "', " +
                    "room_number = " + newRoomNumber + ", " +
                    "contact_number = '" + newContactNumber + "' " +
                    "WHERE reservation_id = " + reservationId;

            try (Statement statement = connection.createStatement()) {
                int affectedRows = statement.executeUpdate(sql);

                if (affectedRows > 0) {
                    System.out.println("Reservation updated successfully!");
                } else {
                    System.out.println("Reservation update failed.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void deleteReservation ( Connection connection, Scanner scanner ) {

        try {
            System.out.print("Enter reservation ID to delete: ");
            int reservationId = scanner.nextInt();

            if (!reservationExists(connection, reservationId)) {
                System.out.println("Reservation not found for the given ID.");
                return;
            }

            String sql = "DELETE FROM reservations WHERE reservation_id = " + reservationId;
            try (Statement statement = connection.createStatement()) {
                int affectedRows = statement.executeUpdate(sql);

                if (affectedRows > 0) {
                    System.out.println("Reservation deleted successfully!");
                } else {
                    System.out.println("Reservation deletion failed.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static boolean reservationExists(Connection connection, int reservationId) {
        try {
            String sql = "SELECT reservation_id FROM reservations WHERE reservation_id = " + reservationId;

            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {

                return resultSet.next(); // If there's a result, the reservation exists
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Handle database errors as needed
        }
    }


    public static void exit() throws InterruptedException {
        System.out.print("Exiting System");
        int i = 5;
        while(i!=0){
            System.out.print(".");
            Thread.sleep(1000);
            i--;
        }
        System.out.println();
        System.out.println("ThankYou For Using Hotel Reservation System!!!");
    }

}
