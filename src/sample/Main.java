package sample;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.rmi.activation.ActivationGroupDesc;
import java.util.HashMap;
import java.util.stream.Collectors;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Stage window = primaryStage;
        //Scene

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(8);
        grid.setHgap(10);
        Text scenetitle = new Text("Welcome");
        scenetitle.setId("welcome-text");
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(scenetitle, 0, 0, 2, 1);
        //Name
        Label name = new Label("E-Mail:");
        grid.add(name, 0, 1);
        TextField name_field = new TextField();
        grid.add(name_field, 1, 1);

        //Password
        Label password = new Label("Password:");
        grid.add(password, 0, 2);
        PasswordField password_field = new PasswordField();
        grid.add(password_field, 1, 2);
        password_field.setPromptText("password");

        Button register = new Button("Register");
        register.setOnAction(actionEvent -> {
            Scene scene2 = getScene2(primaryStage, name_field.getText());
            System.out.println("Email: " + name_field.getText() + "\nPassword:" + password_field.getText());
            window.setScene(scene2);
        });

        grid.add(register, 1, 4);


        Scene scene = new Scene(grid, 300, 300);
        //Did use some code from the java oracle css styling website
        scene.getStylesheets().add("login.css");
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    private Scene getScene2(Stage primaryStage, String name) {
        //Second Scene
        primaryStage.setTitle("Dapper Codes ft Rom Spam Detector ");
        TableView<TestFile> table;
        Label n = new Label("E-Mail:");
        Text text = new Text();
        text.setFill(Color.WHITE);
        n.setLabelFor(text);
        text.setText(name);


        //Student id col
        TableColumn<TestFile, String> fileNameCol = new TableColumn<>("File Name");
        fileNameCol.setMinWidth(300);
        fileNameCol.setCellValueFactory(new PropertyValueFactory<>("filename"));

        TableColumn<TestFile, String> actualClassCol = new TableColumn<>("Actual Class");
        actualClassCol.setMinWidth(300);
        actualClassCol.setCellValueFactory(new PropertyValueFactory<>("actualClass"));

        TableColumn<TestFile, String> spamProbabilityCol = new TableColumn<>("Spam Probability");
        spamProbabilityCol.setMinWidth(300);
        spamProbabilityCol.setCellValueFactory(new PropertyValueFactory<>("prob"));

        Label accuracyLabel = new Label("Accuracy:");

        Text t = new Text();
        t.setFill(Color.WHITE);
        accuracyLabel.setLabelFor(t);
        t.setText(String.valueOf(TestFile.accuracy));

        Label precLabel = new Label("Precision:");
        Text p = new Text();
        p.setFill(Color.WHITE);
        p.setText(String.valueOf(TestFile.precision));
        precLabel.setLabelFor(p);


        table = new TableView<>();
        table.setItems(TestFile.getAllMarks());
        table.getColumns().addAll(fileNameCol, actualClassCol, spamProbabilityCol);
        table.setStyle("-fx-background-color: transparent");

        VBox root = new VBox(5);
        root.setPadding(new Insets(5));
        root.setSpacing(10);
        root.getChildren().addAll(n, text, table, accuracyLabel, t, precLabel, p);

        Scene scene = new Scene(root, 900, 600);
        //Did use some code for stackoverflow.com
        scene.getStylesheets().add("Viper.css");
        return scene;
    }

    private Node createBorderedText(String text) {
        final HBox hbox = new HBox();
        hbox.getChildren().add(new Text(text));
        hbox.setStyle("-fx-border-color: red;");
        return hbox;
    }


    public static void main(String[] args) {
        String[] arguments = {"src/sample/PrSW_i.txt", "src/sample/test/ham", "src/sample/test/spam"};
        TestFile.main(arguments);
        launch(args);
    }
}
