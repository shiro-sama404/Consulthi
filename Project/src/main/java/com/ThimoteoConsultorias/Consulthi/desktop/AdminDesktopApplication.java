package com.ThimoteoConsultorias.Consulthi.desktop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.ThimoteoConsultorias.Consulthi.ConsulthiApplication;

public class AdminDesktopApplication extends Application
{
    private ConfigurableApplicationContext springContext;
    private Parent rootNode;
    
    @Override
    public void init() throws Exception
    {
        springContext = SpringApplication.run(ConsulthiApplication.class);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/desktop/AdminDashboard.fxml"));
        
        fxmlLoader.setControllerFactory(springContext::getBean); 
        
        rootNode = fxmlLoader.load();
    }

    @Override
    public void start(Stage stage) throws Exception
    {
        stage.setTitle("ConsulThi - Desktop Admin");
        Scene scene = new Scene(rootNode, 800, 600);
        stage.setScene(scene);
        stage.show();
    }
    
    @Override
    public void stop() throws Exception
    {
        springContext.close();
    }
    
    public static void main(String[] args)
    {
        launch(args);
    }
}