package com.ThimoteoConsultorias.Consulthi.desktop;

import com.ThimoteoConsultorias.Consulthi.model.User;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import jakarta.annotation.PostConstruct; 


@Component
public class AdminDesktopController
{
    private final AdminApiRestClient adminApiRestClient;
    
    // Tabela de Profissionais Pendentes
    @FXML private TableView<User> pendingUsersTable;
    @FXML private TableColumn<User, Long> pendingIdCol;
    @FXML private TableColumn<User, String> pendingNameCol;
    @FXML private TableColumn<User, String> pendingUsernameCol;
    @FXML private TableColumn<User, String> pendingRoleCol;

    // Tabela de Todos os Usuários
    @FXML private TableView<User> allUsersTable;
    @FXML private TableColumn<User, Long> allIdCol;
    @FXML private TableColumn<User, String> allNameCol;
    @FXML private TableColumn<User, String> allUsernameCol;
    @FXML private TableColumn<User, String> allRoleCol;
    
    private ObservableList<User> pendingUsersData = FXCollections.observableArrayList();
    private ObservableList<User> allUsersData = FXCollections.observableArrayList();

    public AdminDesktopController(AdminApiRestClient adminApiRestClient)
    {
        this.adminApiRestClient = adminApiRestClient;
    }

    /**
     * Inicialização do Controller JavaFX (após a injeção FXML).
     */
    @FXML
    public void initialize()
    {
        // Configura as colunas para mapear as propriedades da classe User
        pendingIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        pendingNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        pendingUsernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        pendingRoleCol.setCellValueFactory(new PropertyValueFactory<>("rolesAsString")); 
        pendingUsersTable.setItems(pendingUsersData);
        
        allIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        allNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        allUsernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        allRoleCol.setCellValueFactory(new PropertyValueFactory<>("rolesAsString"));
        allUsersTable.setItems(allUsersData);
    }
    
    /**
     * Chamado após a construção e injeção do Spring.
     */
    @PostConstruct
    public void initData()
    {
        // Carrega os dados em uma thread separada (pra não travar a GUI)
        loadData();
    }
    
    /**
     * Recarrega todos os dados das tabelas a partir da API REST.
     */
    private void loadData()
    {
        new Thread(() -> {
            try 
            {
                List<User> pending = adminApiRestClient.getPendingProfessionals(); 
                List<User> all = adminApiRestClient.findAllUsers();

                // Atualiza a interface gráfica na thread principal do JavaFX
                Platform.runLater(() -> {
                    pendingUsersData.setAll(pending);
                    allUsersData.setAll(all);
                });
            }
            catch (Exception e)
            {
                Platform.runLater(() -> showErrorAlert("Erro de Comunicação", "Não foi possível conectar ao backend Spring Boot: " + e.getMessage()));
            }
        }).start();
    }
    
    /**
     * Processa o evento de aprovação de profissional (RF01).
     */
    @FXML
    private void handleApproveProfessional()
    {
        User selectedUser = pendingUsersTable.getSelectionModel().getSelectedItem();
        
        if (selectedUser == null)
        {
            showErrorAlert("Erro de Seleção", "Por favor, selecione um profissional na tabela de pendentes.");
            return;
        }
        
        try 
        {
            adminApiRestClient.approveProfessionalRegistration(selectedUser.getId());
            showSuccessAlert("Sucesso", "Profissional " + selectedUser.getFullName() + " aprovado.");
        } 
        catch (Exception e) 
        {
            showErrorAlert("Erro de Aprovação", "Falha ao aprovar o profissional: " + e.getMessage());
        }
        
        loadData(); // Recarrega os dados (remove da lista pendente)
    }

    /**
     * Processa o evento de remoção de usuário (RF08).
     */
    @FXML
    private void handleRemoveUser()
    {
        User selectedUser = allUsersTable.getSelectionModel().getSelectedItem();
        
        if (selectedUser == null)
        {
            showErrorAlert("Erro de Seleção", "Por favor, selecione um usuário na tabela de todos os usuários.");
            return;
        }
        
        Alert confirmation = new Alert(AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmação de Exclusão (RF08)");
        confirmation.setHeaderText("Remoção Permanente de Usuário");
        confirmation.setContentText("Você tem certeza que deseja remover permanentemente o usuário '" + selectedUser.getFullName() + "' (ID: " + selectedUser.getId() + ")? Esta ação é irreversível.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) 
        {
             try 
             {
                 adminApiRestClient.removeUser(selectedUser.getId());
                 showSuccessAlert("Sucesso", "Usuário " + selectedUser.getFullName() + " removido (RF08).");
             } catch (Exception e) {
                 showErrorAlert("Erro de Exclusão", "Falha ao remover o usuário: " + e.getMessage());
             }
             loadData();
        }
    }
    
    private void showErrorAlert(String title, String content) 
    {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showSuccessAlert(String title, String content) 
    {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}