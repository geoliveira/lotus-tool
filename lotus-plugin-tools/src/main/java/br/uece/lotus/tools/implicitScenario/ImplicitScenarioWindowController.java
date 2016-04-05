/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uece.lotus.tools.implicitScenario;

import br.uece.lotus.Component;
import br.uece.lotus.viewer.ComponentViewImpl;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

/**
 *
 * @author Bruno Barbosa
 */

public class ImplicitScenarioWindowController implements Initializable{
    
    @FXML
    private ScrollPane mScrollPane;

    @FXML
    private TableView<ScenarioTableView> mTableView;
    
    private ComponentViewImpl mViewer;
    private ObservableList<ScenarioTableView> data = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
     
        mViewer = new ComponentViewImpl();
        mScrollPane.setContent(mViewer);
        
        Component component = (Component) resources.getObject("component");
        mViewer.setComponent(component);
        data.addAll(new ScenarioTableView("a,b,c,d"),new ScenarioTableView("a,c,b,e"));
        iniciarTable();
    }    

    private void iniciarTable() {
        
        TableColumn scenario = new TableColumn("Implicit Scenario");
        scenario.setCellValueFactory(new PropertyValueFactory<>("implicitScenario"));
        scenario.setPrefWidth(500);
        
        TableColumn operation = new TableColumn("Operation");
        operation.setCellValueFactory(new PropertyValueFactory<>("Operation"));
        operation.setPrefWidth(90);
        
        Callback<TableColumn<ScenarioTableView,String>,TableCell<ScenarioTableView,String>> cellFactory = new Callback<TableColumn<ScenarioTableView, String>, TableCell<ScenarioTableView,String>>() {
            @Override
            public TableCell call(TableColumn<ScenarioTableView, String> param) {
                final TableCell<ScenarioTableView,String> cell = new TableCell<ScenarioTableView,String>(){
                    final HBox box = new HBox(5);
                    final Button btnOk = new Button();
                    final Button btnCancel = new Button();
                    
                    @Override
                    public void updateItem( String item, boolean empty ){
                        super.updateItem(item, empty);
                        if(empty){
                            setGraphic(null);
                            setText(null);
                        }else{
                            String selecionado = getTableView().getItems().get(getIndex()).implicitScenarioProperty().get();
                            btnOk.setOnAction((ActionEvent event) -> {
                                acaoOK(selecionado);
                            });
                            btnOk.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/image/Ok_Scenario.png"))));
                            btnCancel.setOnAction((ActionEvent event) -> {
                                acaoCancel(selecionado);
                            });
                            btnCancel.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/image/Cancel_Scenario.png"))));
                            box.getChildren().addAll(btnOk,btnCancel);
                            setGraphic(box);
                            setText(null);
                        }
                    }
                };
                return cell;
            }
        };
        
        operation.setCellFactory(cellFactory);
        
        mTableView.setItems(data);
        mTableView.getColumns().addAll(scenario,operation);
    }
    
    private void acaoOK(String cenarioSelecionado){
        System.out.println("ativou o botao ok, cenario: "+cenarioSelecionado);
    }
    
    private void acaoCancel(String cenarioSelecionado){
        System.out.println("ativou o botao cancel, cenario: "+cenarioSelecionado);
    }
    
}
