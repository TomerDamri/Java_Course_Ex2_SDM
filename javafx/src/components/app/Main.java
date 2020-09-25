package components.app;

import java.net.URL;

import components.mapComponent.MapController;
import components.placeOrderComponent.PlaceOrderController;
import components.sdmComponent.SDMController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import logic.BusinessLogic;

public class Main extends Application {

    @Override
    public void start (Stage primaryStage) throws Exception {

        FXMLLoader fxmlLoader = new FXMLLoader();
        URL url = getClass().getResource("/components/sdmComponent/SDM.fxml");
        fxmlLoader.setLocation(url);
        ScrollPane sdmComponent = fxmlLoader.load(url.openStream());
        SDMController sdmController = fxmlLoader.getController();

        // load place order component and controller from fxml
        fxmlLoader = new FXMLLoader();
        url = getClass().getResource("/components/placeOrderComponent/placeOrder.fxml");
        fxmlLoader.setLocation(url);
        BorderPane placeOrderComponent = fxmlLoader.load(url.openStream());
        PlaceOrderController placeOrderController = fxmlLoader.getController();

        // load place order component and controller from fxml
        fxmlLoader = new FXMLLoader();
        url = getClass().getResource("/components/mapComponent/Map.fxml");
        fxmlLoader.setLocation(url);
        ScrollPane mapComponent = fxmlLoader.load(url.openStream());
        MapController mapController = fxmlLoader.getController();

        // load sdm component and controller from fxml

        fxmlLoader = new FXMLLoader();
        url = getClass().getResource("/components/app/app.fxml");
        fxmlLoader.setLocation(url);
        ScrollPane root = fxmlLoader.load(url.openStream());
        AppController appController = fxmlLoader.getController();

        appController.setMapComponentController(mapController);
        appController.setPlaceOrderComponentController(placeOrderController);
        appController.setSdmComponentController(sdmController);

        BusinessLogic businessLogic = new BusinessLogic(appController);
        root = sdmComponent;

        // sdmController.setPrimaryStage(primaryStage);
        sdmController.setBusinessLogic(businessLogic);
        sdmController.setPlaceOrderPane(placeOrderComponent);
        sdmController.setMapScrollPane(mapComponent);
        sdmController.setMapGridPane(mapController.getLocationsGridPane());
        sdmController.setAppController(appController);

        // set stage
        primaryStage.setTitle("Super Duper Market");
        Scene scene = new Scene(root, 780   , 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main (String[] args) {
        launch(args);
    }
}
