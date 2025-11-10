package com.ThimoteoConsultorias.Consulthi.desktop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.ThimoteoConsultorias.Consulthi.ConsulthiApplication; // Sua classe principal

public class AdminDesktopApplication extends Application
{
    private ConfigurableApplicationContext springContext;
    private Parent rootNode;
    
    @Override
    public void init() throws Exception
    {
        // 1. Inicializa o Spring Boot (o backend)
        springContext = SpringApplication.run(ConsulthiApplication.class);

        // 2. Carrega o FXML e injeta dependências do Spring no Controller JavaFX
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/desktop/AdminDashboard.fxml"));
        // Usa o contexto Spring para instanciar o Controller
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
        // Garante que o Spring Boot seja encerrado ao fechar a janela.
        springContext.close();
    }
    
    public static void main(String[] args)
    {
        launch(args);
    }
}