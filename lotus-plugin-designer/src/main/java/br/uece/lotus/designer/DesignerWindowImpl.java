/*
 * The MIT License
 *
 * Copyright 2014 Universidade Estadual do CearÃ¡.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package br.uece.lotus.designer;

import br.uece.lotus.BigState;
import br.uece.lotus.Component;
import br.uece.lotus.State;
import br.uece.lotus.Transition;
import br.uece.lotus.properties.TransitionsPropertiesController;
import br.uece.lotus.viewer.*;
import br.uece.seed.app.ExtensibleFXToolbar;
import br.uece.seed.app.ExtensibleToolbar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;

import javax.swing.JOptionPane;

/**
 * @author emerson
 */
public class DesignerWindowImpl extends AnchorPane implements DesignerWindow {

    private final TextField txtLabel;
    private final TextField txtGuard;
    private final TextField txtProbability;
    Rectangle ultimoRetanguloAdicionado;
    double coordenadaInicialX, coordenadaInicialY, coordenadaFinalX, coordenadaFinalY,
            coornenadaIstanteX, coordenadaIstanteY;
    boolean shifPrecionado = false;
    boolean StatesSelecionadoPeloRetangulo = false;
    boolean ctrlPressionado = false;
    double posicaoDoEstadoXMaisRaio;
    double posicaoDoEstadoYMaisRaio;
    double posicaoDoEstadoXMenosRaio;
    double posicaoDoEstadoYMenosRaio;
    ArrayList<State> stateDentroDoRetangulo = new ArrayList<State>();
    double posCircleX;
    double posCircleY;
    double variacaoDeX;
    double variacaoDeY;
    double stateDoPrimeiroClickX, stateDoPrimeiroClickY;
    boolean caso1, caso2, caso3, caso4, retorno, aux, modoCriacaoDoRetangulo = false;
    static final int RAIO_CIRCULO = 15;

    public static final int MODO_NENHUM = 0;
    public static final int MODO_VERTICE = 1;
    public static final int MODO_TRANSICAO = 2;
    public static final int MODO_REMOVER = 3;
    public static final int MODO_MOVER = 4;
    private int contID = -1;
    private final ComponentView mViewer;
    private final ToolBar mToolbar;
    private final ToggleGroup mToggleGroup;
    private final ToggleButton mBtnBigState;
    private final ToggleButton mBtnArrow;
    private final ToggleButton mBtnState;
    private final ToggleButton mBtnTransitionLine;
    private final ToggleButton mBtnTransitionArc;
    private final ToggleButton mBtnEraser;
    private final ToggleButton mBtnHand;
    private final MenuButton mBtnZoom;
    private final Button mBtnUndo;
    private final Button mBtnRedo;
    private ComponentView[] mUndoRedo;
    private final int tamHistorico = 14;
    private final ImageView iconBigState = new ImageView(new Image(getClass().getResourceAsStream("/images/ic_big_state.png")));
    private final ImageView iconBigStateDismount = new ImageView(new Image(getClass().getResourceAsStream("/images/ic_big_state_dismount.png")));

    private final ToolBar mStateToolbar;
    private final ToolBar mTransitionToolbar;
    private final ExtensibleToolbar mExtensibleStateToolbar;
    private final ExtensibleToolbar mExtensibleTransitionToolbar;

    private final ScrollPane mScrollPanel;
    private boolean mExibirPropriedadesTransicao;

    public ComponentView getMViewer() {
        return this.mViewer;
    }

    private ComponentView.Listener mViewerListener = new ComponentView.Listener() {
        @Override
        public void onStateViewCreated(ComponentView cv, StateView v) {

        }

        @Override
        public void onTransitionViewCreated(ComponentView v, TransitionView tv) {
            if (mExibirPropriedadesTransicao) {
                setComponenteSelecionado(tv);
            }
            mExibirPropriedadesTransicao = false;

        }
    };
    private String mDefaultTransitionColor;
    private String mDefaultTransitionTextColor;
    private Integer mDefaultTransitionWidth;
    private String mDefaultTransitionLabel;
    private EventHandler<ActionEvent> mSetStateAsInitial = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            if (mComponentSelecionado == null) {
                return;
            }
            State s = ((StateView) mComponentSelecionado).getState();
            s.setError(false);
            s.setFinal(false);
            s.setAsInitial();
            s.setColor(null);
        }
    };
    private EventHandler<ActionEvent> mSetStateAsNormal = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            if (mComponentSelecionado == null) {
                return;
            }
            State s = ((StateView) mComponentSelecionado).getState();
            s.setFinal(false);
            s.setError(false);
            s.setColor(null);
        }
    };

    private EventHandler<ActionEvent> mSetStateAsError = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            if (mComponentSelecionado == null) {
                return;
            }
            State s = ((StateView) mComponentSelecionado).getState();
            if(s.isInitial()){
                JOptionPane.showMessageDialog(null, "Impossible to change an Initial state to Error.", "Alert", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Component c = mViewer.getComponent();

            s.setFinal(false);
            c.setErrorState(s);
            s.setColor(null);
        }
    };

    private EventHandler<ActionEvent> mSetStateAsFinal = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            if (mComponentSelecionado == null) {
                return;
            }
            State s = ((StateView) mComponentSelecionado).getState();
            if(s.isInitial()){
                JOptionPane.showMessageDialog(null, "Impossible to change an Initial state to Final.", "Alert", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Component c = mViewer.getComponent();

            s.setError(false);
            c.setFinalState(s);
            s.setColor(null);
        }
    };

    private EventHandler<ActionEvent> mCreateDismountBigState = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            if (mComponentSelecionado == null) {
                return;
            }
            //CRIANDO BIGSTATE - USERDATA            
            BigState bigState = new BigState();
            List<State> listaS = statesSelecionados;

            //TEST DISMOUNT BIGSTATE
            if(statesSelecionados.size()==1){
                BigState bigS = (BigState) statesSelecionados.get(0).getValue("bigstate");
                if (bigS!= null){
                    if (!bigS.dismountBigState(mViewer.getComponent())){
                        JOptionPane.showMessageDialog(null, "You need another BigState before dismantling");
                        return;
                    }
                    mBtnBigState.setSelected(false);
                    mBtnBigState.setGraphic(iconBigState);
                    mViewer.getComponent().remove(statesSelecionados.get(0));
                    return;
                }
            }

            if (!bigState.addStatesAndTransition(listaS)){
                JOptionPane.showMessageDialog(null, "Add more States or initial State selected","Attention", JOptionPane.WARNING_MESSAGE);
                return;
            }

            //CRIANDO NA TELA O STATE MAIOR COM O BIGSTATE
            if (contID == -1) {
                updateContID();
            }
            int id = mViewer.getComponent().getStatesCount();
            State novoState = mViewer.getComponent().newState(id);
            novoState.setID(contID);
            contID++;
            novoState.setValue("bigstate", bigState);
            novoState.setLayoutX(statesSelecionados.get(0).getLayoutX()+20);
            novoState.setLayoutY(statesSelecionados.get(0).getLayoutY()+20);
            novoState.setLabel(String.valueOf(id));
            novoState.setBig(true);
            bigState.setState(novoState);

            //ADD TRANSITIONS DOS BIGSTATES
            int type = 0;
            for (Transition t : bigState.getListaTransitionsForaSaindo()) {
                if (novoState.getTransitionsTo(t.getDestiny()).size() == 0) {
                    Transition tNova = mViewer.getComponent().buildTransition(novoState, t.getDestiny())
                            .setValue("view.type", type)
                            .setLabel(t.getLabel() == null || t.getLabel().equals("") ? "" : t.getLabel())
                            .create();
                } else {
                    String labelAntiga = novoState.getTransitionTo(t.getDestiny()).getLabel();
                    novoState.getTransitionTo(t.getDestiny())
                            .setLabel(t.getLabel() == null || t.getLabel().equals("") ? labelAntiga : labelAntiga + ", " + t.getLabel());

                }
            }
            for (Transition t : bigState.getListaTransitionsForaChegando()) {
                if(novoState.getTransitionsTo(t.getSource()).size()!=0)
                    type = 1;
                if (t.getSource().getTransitionsTo(novoState).size() == 0) {
                    Transition tNova = mViewer.getComponent().buildTransition(t.getSource(), novoState)
                            .setValue("view.type", type)
                            .setLabel(t.getLabel() == null || t.getLabel().equals("") ? "" : t.getLabel())
                            .create();
                } else {
                    String labelAntiga = t.getSource().getTransitionTo(novoState).getLabel();
                    t.getSource().getTransitionTo(novoState)
                            .setLabel(t.getLabel() == null || t.getLabel().equals("") ? labelAntiga : labelAntiga + ", " + t.getLabel());
                }
            }

            mBtnBigState.setSelected(true);
            mBtnBigState.setGraphic(iconBigStateDismount);

            BigState.removeStatesComponent();
        }
    };

    private int mTransitionViewType;

    @Override
    public ExtensibleToolbar getTransitionContextToolbar() {
        return mExtensibleTransitionToolbar;
    }

    @Override
    public ExtensibleToolbar getStateContextToolbar() {
        return mExtensibleStateToolbar;
    }

    @Override
    public void setDefaultTransitionLabel(String label) {
        mDefaultTransitionLabel = label;
    }

    @Override
    public void setDefaultTransitionWidth(Integer width) {
        mDefaultTransitionWidth = width;
    }

    @Override
    public void setDefaultTransitionTextColor(String color) {
        mDefaultTransitionTextColor = color;
    }

    @Override
    public void setDefaultTransitionColor(String color) {
        mDefaultTransitionColor = color;
    }

    @Override
    public String getTitle() {
        Component c = mViewer.getComponent();
        return c.getName();
    }

    @Override
    public Node getNode() {
        return this;
    }

    public interface Listener {

        void onSelectionChange(DesignerWindowImpl v);
    }

    private int mModoAtual;
    private final ContextMenu mComponentContextMenu;
    //seleÃ§Ã£o e destaque
    private Object mComponentSobMouse;
    private Object mComponentSelecionado;
    private final List<Listener> mListeners = new ArrayList<>();
    //zoom e mover
    private double mViewerScaleXPadrao, mViewerScaleYPadrao, mViewerTranslateXPadrao, mViewerTranslateYPadrao;
    private double posicaoMViewerHandX = 0, posicaoMViewerHandY = 0;//posição mviewer
    private double mouseHandX = 0, mouseHandY = 0;// posiÃ§Ã£o mouse
    private CheckBox zoomReset;
    private DoubleProperty zoomFactor = new SimpleDoubleProperty(1);
    private Scale escala = new Scale(1, 1);
    private HBox paleta;

    public DesignerWindowImpl(ComponentView viewer) {
        mViewer = viewer;
        mUndoRedo = new ComponentView[14];

        mToolbar = new ToolBar();
        mToggleGroup = new ToggleGroup();
        mBtnArrow = new ToggleButton();
        mBtnArrow.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/images/ic_arrow.png"))));
        mBtnArrow.setOnAction((ActionEvent e) -> {
            // ComponentViewImpl v = new ComponentViewImpl();
            // viewer.reajuste();
            setModo(MODO_NENHUM);
        });
        mBtnArrow.setToggleGroup(mToggleGroup);
        mBtnState = new ToggleButton();
        mBtnState.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/images/ic_state.png"))));
        mBtnState.setOnAction((ActionEvent e) -> {
            setModo(MODO_VERTICE);
        });
        mBtnState.setToggleGroup(mToggleGroup);
        mBtnTransitionLine = new ToggleButton();
        mBtnTransitionLine.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/images/ic_transition_line.png"))));
        mBtnTransitionLine.setOnAction((ActionEvent e) -> {
            mTransitionViewType = TransitionView.Geometry.LINE;
            setModo(MODO_TRANSICAO);
        });
        mBtnTransitionLine.setToggleGroup(mToggleGroup);

        mBtnTransitionArc = new ToggleButton();
        mBtnTransitionArc.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/images/ic_transition_semicircle.png"))));
        mBtnTransitionArc.setOnAction((ActionEvent e) -> {
            mTransitionViewType = TransitionView.Geometry.CURVE;
            setModo(MODO_TRANSICAO);
        });
        mBtnTransitionArc.setToggleGroup(mToggleGroup);

        mBtnEraser = new ToggleButton();
        mBtnEraser.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/images/ic_eraser.png"))));
        mBtnEraser.setOnAction((ActionEvent e) -> {
            setModo(MODO_REMOVER);
        });
        mBtnEraser.setToggleGroup(mToggleGroup);

        mBtnHand = new ToggleButton();
        mBtnHand.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/images/ic_hand.png"))));
        mBtnHand.setToggleGroup(mToggleGroup);
        mBtnHand.setOnAction((ActionEvent e) -> {
            setModo(MODO_MOVER);
        });

        mBtnBigState = new ToggleButton();
        mBtnBigState.setGraphic(iconBigState);
        mBtnBigState.setOnAction(mCreateDismountBigState);

        mBtnZoom = new MenuButton();
        HBox menuSlideZoom = new HBox();
        menuSlideZoom.setSpacing(5);
        Slider zoomSlide = new Slider(0.5, 2, 1);
        zoomSlide.setShowTickMarks(true);
        ImageView zoomMoree = new ImageView(new Image(getClass().getResourceAsStream("/images/ic_zoom_mais.png")));
        ImageView zoomLess = new ImageView(new Image(getClass().getResourceAsStream("/images/ic_zoom_menos.png")));
        zoomReset = new CheckBox("Reset");
        menuSlideZoom.getChildren().addAll(zoomLess, zoomSlide, zoomMoree, zoomReset);
        mBtnZoom.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/images/ic_zoom.png"))));
        MenuItem zoomHBox = new MenuItem();
        zoomHBox.setGraphic(menuSlideZoom);
        zoomFactor.bind(zoomSlide.valueProperty());
        zoomFactor.addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            escala.setX(newValue.doubleValue());
            escala.setY(newValue.doubleValue());
            requestLayout();
            if (zoomFactor.getValue() != 1)
                zoomReset.setSelected(false);
        });
        mBtnZoom.getItems().add(zoomHBox);

        mBtnUndo = new Button();
        mBtnUndo.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/images/ic_undo.png"))));
        mBtnUndo.setOnAction((ActionEvent event) -> {
            historicoViewer("Desfazer");
        });

        mBtnRedo = new Button();
        mBtnRedo.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/images/ic_redo.png"))));
        mBtnRedo.setOnAction((ActionEvent event) -> {
            historicoViewer("Refazer");
        });

        //Set Colors-----------------------------------------------------------------------------------
        ColorPicker cores = new ColorPicker();
        MenuButton complementoColors = new MenuButton("");
        cores.setOnAction((ActionEvent event) -> {
            if(statesSelecionados.isEmpty()){
                System.out.println("é nulo o statesSelecionados");
                changeColorsState(cores, "");
            }else{
                changeColorsState(cores, "MultiSelecao");
            }
        });
        MenuItem defaultColor = new MenuItem("Default Color");
        defaultColor.setOnAction((ActionEvent event) -> {
            changeColorsState(cores, "Default");
        });
        complementoColors.getItems().add(defaultColor);
        paleta = new HBox();
        paleta.setAlignment(Pos.CENTER);
        paleta.getChildren().addAll(cores,complementoColors);
        paleta.setVisible(false);


        txtLabel = new TextField();
        txtLabel.setPromptText("Action");
        txtLabel.setOnKeyReleased(event -> {
            Object obj = getSelectedView();
            if (obj instanceof TransitionView) {
                ((TransitionView) obj).getTransition().setLabel(txtLabel.getText());
            }
        });
        txtGuard = new TextField();
        txtGuard.setPromptText("Guard");
        // txtGuard.setOnAction(event -> {
        txtGuard.setOnKeyReleased(event -> {
            Object obj = getSelectedView();
            if (obj instanceof TransitionView) {
                ((TransitionView) obj).getTransition().setGuard(txtGuard.getText());
            }
        });
        txtProbability = new TextField();
        txtProbability.setPrefWidth(50);
        txtProbability.setAlignment(Pos.CENTER);
        TransitionsPropertiesController.campoProbability(txtProbability);
        txtProbability.setPromptText("%");
        txtProbability.setOnAction(event -> {
            Object obj = getSelectedView();
            if (obj instanceof TransitionView) {
                try {
                    if(txtProbability.getText().equals("")){
                        ((TransitionView) obj).getTransition().setProbability(null);
                    }else{
                        String valorDoField = txtProbability.getText().trim();
                        String auxValor = "";
                        if(valorDoField.contains(",")){
                            auxValor = valorDoField.replaceAll(",", ".");
                            double teste = Double.parseDouble(auxValor);
                            if(teste<0 || teste >1){
                                JOptionPane.showMessageDialog(null, "Input probability between 0 and 1", "Erro", JOptionPane.ERROR_MESSAGE);
                                auxValor="";
                                txtProbability.setText("");
                            }
                        }
                        else if(valorDoField.contains(".")){
                            auxValor = valorDoField;
                            double teste = Double.parseDouble(auxValor);
                            if(teste<0 || teste >1){
                                JOptionPane.showMessageDialog(null, "Imput probability need 0 to 1", "Erro", JOptionPane.ERROR_MESSAGE);
                                auxValor="";
                                txtProbability.setText("");
                            }
                        }
                        else if(valorDoField.contains("%")){
                            double valorEntre0e1;
                            auxValor = valorDoField.replaceAll("%", "");
                            valorEntre0e1 = (Double.parseDouble(auxValor))/100;
                            auxValor = String.valueOf(valorEntre0e1);
                            double teste = Double.parseDouble(auxValor);
                            if(teste<0 || teste >1){
                                JOptionPane.showMessageDialog(null, "Imput probability need 0 to 1", "Erro", JOptionPane.ERROR_MESSAGE);
                                auxValor="";
                                txtProbability.setText("");
                            }
                        }
                        else{
                            if(valorDoField.equals("0") || valorDoField.equals("1")){
                                auxValor = valorDoField;
                            }else{
                                JOptionPane.showMessageDialog(null, "Imput probability need 0 to 1", "Erro", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                        ((TransitionView) obj).getTransition().setProbability(Double.parseDouble(auxValor));
                    }
                } catch (Exception e) {
                    //ignora
                }
            }
        });

        addListener(v -> {
            Object obj = v.getSelectedView();
            if (obj instanceof TransitionView) {
                Transition t = ((TransitionView) obj).getTransition();
                txtGuard.setText(t.getGuard());
                txtLabel.setText(t.getLabel());
                txtProbability.setText(t.getProbability() == null ? "" : t.getProbability().toString());
            }
        });

        //ToolTips
        Tooltip arrowInfo = new Tooltip("Selection");
        Tooltip stateInfo = new Tooltip("State");
        Tooltip lineTransitionInfo = new Tooltip("Straight Transition");
        Tooltip arcTransitionInfo = new Tooltip("Curved Transition");
        Tooltip eraserInfo = new Tooltip("Eraser");
        Tooltip handInfo = new Tooltip("Move");
        Tooltip zoomInfo = new Tooltip("Ctrl + MouseScroll ↑\nCtrl + MouseScroll ↓\nCtrl + Mouse Button Middle");
        Tooltip bigStateInfo = new Tooltip("Composed State");
        Tooltip undoInfo = new Tooltip("Undo (CTRL+Z");
        Tooltip redoInfo = new Tooltip("Redo (CTRL+Y");
        Tooltip.install(mBtnArrow, arrowInfo);
        Tooltip.install(mBtnState, stateInfo);
        Tooltip.install(mBtnTransitionLine, lineTransitionInfo);
        Tooltip.install(mBtnTransitionArc, arcTransitionInfo);
        Tooltip.install(mBtnEraser, eraserInfo);
        Tooltip.install(mBtnHand, handInfo);
        Tooltip.install(mBtnZoom, zoomInfo);
        Tooltip.install(mBtnBigState, bigStateInfo);
        Tooltip.install(mBtnUndo, undoInfo);
        Tooltip.install(mBtnRedo, redoInfo);

        mToolbar.getItems().addAll(mBtnArrow, mBtnState, mBtnTransitionLine, mBtnTransitionArc, mBtnEraser, mBtnHand, mBtnZoom, mBtnBigState,
                new Separator(Orientation.VERTICAL),paleta);//mBtnUndo, mBtnRedo); //, new Separator(), txtGuard, txtProbability, txtLabel);

        mStateToolbar = new ToolBar();
        mStateToolbar.setVisible(false);
        mTransitionToolbar = new ToolBar();
        mTransitionToolbar.setVisible(false);
//        getChildren().add(mStateToolbar);
//        getChildren().add(mTransitionToolbar);
        mExtensibleStateToolbar = new ExtensibleFXToolbar(mStateToolbar);
        mExtensibleTransitionToolbar = new ExtensibleFXToolbar(mTransitionToolbar);

        HBox aux = new HBox(mToolbar, mStateToolbar, mTransitionToolbar);
        getChildren().add(aux);
        AnchorPane.setTopAnchor(aux, 0D);
        AnchorPane.setLeftAnchor(aux, 0D);
        AnchorPane.setRightAnchor(aux, 0D);

        mComponentContextMenu = new ContextMenu();

        mViewer.getNode().getTransforms().add(escala);

        mViewer.addListener(mViewerListener);
        mViewer.setStateContextMenu(mComponentContextMenu);
        mViewer.getNode().setOnMouseClicked(aoClicarMouse);
        mViewer.getNode().setOnMouseMoved(aoMoverMouse);


        mViewer.getNode().setOnDragDetected(aoDetectarDragSobreVertice);
        mViewer.getNode().setOnDragOver(aoDetectarPossivelAlvoParaSoltarODrag);
        mViewer.getNode().setOnDragDropped(aoSoltarMouseSobreVertice);

        mViewer.getNode().setOnMousePressed(aoIniciarArrastoVerticeComOMouse);
        mViewer.getNode().setOnMouseDragged(aoArrastarVerticeComOMouse);
        mViewer.getNode().setOnMouseReleased(aoLiberarVerticeArrastadoComOMouse);

        mViewer.getNode().addEventHandler(KeyEvent.ANY, teclaPressionada);

        //////////////fiz isso/////
        mViewer.tamalhoPadrao();
        mScrollPanel = new ScrollPane(mViewer.getNode());
        AnchorPane.setTopAnchor(mScrollPanel, 44D);
        AnchorPane.setLeftAnchor(mScrollPanel, 0D);
        AnchorPane.setRightAnchor(mScrollPanel, 175D);
        AnchorPane.setBottomAnchor(mScrollPanel, 0D);
        getChildren().add(mScrollPanel);

        //Propriedades
        VBox mPainelPropriedades = new VBox();
        mPainelPropriedades.getChildren().add(new Label("Action"));
        mPainelPropriedades.getChildren().add(txtLabel);
        mPainelPropriedades.getChildren().add(new Label("Guard"));
        mPainelPropriedades.getChildren().add(txtGuard);

        HBox mPainelPropriedadeProbability = new HBox(2);
        mPainelPropriedadeProbability.setAlignment(Pos.CENTER);
        mPainelPropriedades.getChildren().add(new Label("Probability"));
        mPainelPropriedadeProbability.getChildren().add(txtProbability);
        Label exemplo = new Label("Ex: 0,5 OR 0.5 OR 50%\nPress Enter to validate");
        exemplo.setFont(new Font(10));
        mPainelPropriedadeProbability.getChildren().add(exemplo);

        mPainelPropriedades.getChildren().add(mPainelPropriedadeProbability);
        mPainelPropriedades.setPadding(new Insets(5));
        mPainelPropriedades.setSpacing(5);
        AnchorPane.setTopAnchor(mPainelPropriedades, 44D);
        AnchorPane.setRightAnchor(mPainelPropriedades, 0D);
        AnchorPane.setBottomAnchor(mPainelPropriedades, 0D);
        //AnchorPane.setLeftAnchor(mPainelPropriedades, 0D);
        getChildren().add(mPainelPropriedades);

        /*/KeyCode Combination (Erro de nullPoint na scene)
        mBtnUndo.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.Z, KeyCodeCombination.CONTROL_DOWN), new Runnable() {

            @Override
            public void run() {
                mBtnUndo.fire();
                System.out.println("Desfazer keycode");
            }
        });*/

//       mViewer.getNode().minHeightProperty().bind(mScrollPanel.heightProperty());
//       mViewer.getNode().minWidthProperty().bind(mScrollPanel.widthProperty());


        mViewerScaleXPadrao = mViewer.getNode().getScaleX();
        mViewerScaleYPadrao = mViewer.getNode().getScaleY();
        mViewerTranslateXPadrao = mViewer.getNode().getTranslateX();
        mViewerTranslateYPadrao = mViewer.getNode().getTranslateY();

        MenuItem mSetAsInitialMenuItem = new MenuItem("Set as initial");
        mSetAsInitialMenuItem.setOnAction(mSetStateAsInitial);
        MenuItem mSetAsNormalMenuItem = new MenuItem("Set as normal");
        mSetAsNormalMenuItem.setOnAction(mSetStateAsNormal);
        MenuItem mSetAsFinalMenuItem = new MenuItem("Set as error");
        mSetAsFinalMenuItem.setOnAction(mSetStateAsError);
        MenuItem mSetAsErrorMenuItem = new MenuItem("Set as final");
        mSetAsErrorMenuItem.setOnAction(mSetStateAsFinal);

        MenuItem mCreateDismountBigStateMenuItem = new MenuItem("Create/Dismount Composed State");
        mCreateDismountBigStateMenuItem.setOnAction(mCreateDismountBigState);

        MenuItem mSaveAsPNG = new MenuItem("Save as PNG");
        mSaveAsPNG.setOnAction((ActionEvent event) -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save as PNG");
            fileChooser.setInitialFileName(mViewer.getComponent().getName() + ".png");
            fileChooser.setInitialDirectory(
                    new File(System.getProperty("user.home"))
            );
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("PNG Image (*.png)", "*.png")
            );
            File arq = fileChooser.showSaveDialog(null);
            if (arq == null) {
                return;
            }
            mViewer.saveAsPng(arq);
            JOptionPane.showMessageDialog(null, "PNG Image successfuly saved!");
        });
        mViewer.getNode().setOnScroll(zoom);
        mComponentContextMenu.getItems().addAll(mSetAsInitialMenuItem, new SeparatorMenuItem(), mCreateDismountBigStateMenuItem, new SeparatorMenuItem(), mSetAsNormalMenuItem, mSetAsFinalMenuItem, mSetAsErrorMenuItem, new SeparatorMenuItem(), mSaveAsPNG);

        //Resetando Zoom
        zoomReset.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (zoomReset.isSelected()) {
                zoomSlide.setValue(1);
                mViewer.getNode().setScaleX(mViewerScaleXPadrao);
                mViewer.getNode().setScaleY(mViewerScaleYPadrao);
                mViewer.getNode().setTranslateX(mViewerTranslateXPadrao);
                mViewer.getNode().setTranslateY(mViewerTranslateYPadrao);
            }
        });

    }

    ////////////////////////////////////////////////////////////////////////////
    // Ao Clicar o mouse(clickar e soltar)
    ////////////////////////////////////////////////////////////////////////////
    private EventHandler<? super MouseEvent> aoClicarMouse = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent e) {
            if (MouseButton.SECONDARY.equals(e.getButton())) {
                setComponenteSelecionado(mComponentSobMouse);
                if(mComponentSelecionado instanceof StateView){
                    mComponentContextMenu.show(mViewer.getNode(), e.getScreenX(), e.getScreenY());
                }else{
                    mComponentContextMenu.hide();
                }
                return;
            }
            else {
                mComponentContextMenu.hide();
            }

            if (e.isControlDown() && e.getButton() == MouseButton.MIDDLE) {
                mViewer.getNode().setScaleX(mViewerScaleXPadrao);
                mViewer.getNode().setScaleY(mViewerScaleYPadrao);
                mViewer.getNode().setTranslateX(mViewerTranslateXPadrao);
                mViewer.getNode().setTranslateY(mViewerTranslateYPadrao);
            }

            if (mModoAtual == MODO_NENHUM) {

                if (mComponentSobMouse != null && (mComponentSobMouse instanceof StateView)) {
                    paleta.setVisible(true);
                    //VERIFICANDO SE TEM UM BIGSTATE
                    StateView stateView = (StateView) mComponentSobMouse;
                    State state = stateView.getState();
                    BigState bigState = (BigState) state.getValue("bigstate");
                    if (bigState != null) {
                        mBtnBigState.setSelected(true);
                        mBtnBigState.setGraphic(iconBigStateDismount);
                        /*System.out.println("NUMERO DE BIGSTATES = "+BigState.todosOsBigStates.size());
                        System.out.println(((BigState)((StateView)mComponentSobMouse).getState().getValue("bigstate")).toString());*/
                        if (e.getClickCount() == 2) {
                            if (!bigState.dismountBigState(mViewer.getComponent())){
                                JOptionPane.showMessageDialog(null, "You need another BigState before dismantling");
                                return;
                            }
                            mBtnBigState.setSelected(false);
                            mBtnBigState.setGraphic(iconBigState);
                            mViewer.getComponent().remove(state);
                        }
                    }
                    else {
                        mBtnBigState.setSelected(false);
                        mBtnBigState.setGraphic(iconBigState);
                    }
                }else{
                    mBtnBigState.setSelected(false);
                    mBtnBigState.setGraphic(iconBigState);
                    // System.out.println("saiu aqui ?");

                    if(!StatesSelecionadoPeloRetangulo){
                        paleta.setVisible(false);}

                }

            } else {
                if (mModoAtual == MODO_VERTICE) {
                    if (!(mComponentSobMouse instanceof StateView)) {
                        if (contID == -1) {
                            updateContID();
                        }
                        int id = mViewer.getComponent().getStatesCount();
                        State s = mViewer.getComponent().newState(id);
                        s.setID(contID);
                        contID++;
                        s.setLayoutX(e.getX());
                        s.setLayoutY(e.getY());
                        s.setLabel(String.valueOf(id));

                        if (mViewer.getComponent().getStatesCount() == 0) {
                            mViewer.getComponent().setInitialState(s);
                        }
                    }
                } else if (mModoAtual == MODO_REMOVER) {
                    if (mComponentSobMouse instanceof StateView) {
                        State v = ((StateView) mComponentSobMouse).getState();
                        if(v.getValue("bigstate") instanceof BigState){
                            BigState.removeBigState((BigState) v.getValue("bigstate"));
                        }
                        mViewer.getComponent().remove(v);
                    } else if (mComponentSobMouse instanceof TransitionView) {
                        Transition t = ((TransitionView) mComponentSobMouse).getTransition();
                        State iniTransition = t.getSource();
                        State fimTransition = t.getDestiny();
                        mViewer.getComponent().remove(t);
                        //Verificar Mais de uma Trasition do mesmo Source e Destiny
                        List<Transition> multiplasTransicoes = iniTransition.getTransitionsTo(fimTransition);
                        if(multiplasTransicoes.size() > 0){
                            //deletar da tela
                            for(Transition trans : multiplasTransicoes){
                                mViewer.getComponent().remove(trans);
                            }
                            //recriar transitions
                            for(Transition trans : multiplasTransicoes){
                                mViewer.getComponent().buildTransition(iniTransition, fimTransition)
                                        .setGuard(trans.getGuard())
                                        .setLabel(trans.getLabel())
                                        .setProbability(trans.getProbability())
                                        .setViewType(TransitionView.Geometry.CURVE)
                                        .create();
                            }
                        }
                    }
                }
            }
        }
    };

    ////////////////////////////////////////////////////////////////////////////////
// Mover o mouse(mover o cursor do mouse)
////////////////////////////////////////////////////////////////////////////
    private double coordenadaIniX = 0;
    private double coordenadaIniY = 0;
    private EventHandler<? super MouseEvent> aoMoverMouse = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent t) {
            Object aux = getComponentePelaPosicaoMouse(new Point2D(t.getSceneX(), t.getSceneY()));
//            if (mComponentSobMouse != null) {
//                mComponentSobMouse.setHighlighted(false);
//            }
//            if (aux != null) {
//                aux.setHighlighted(true);
//            }
            mComponentSobMouse = aux;
        }
    };
    ////////////////////////////////////////////////////////////////////////////
    // Mover state (clickar sem soltar)
    ////////////////////////////////////////////////////////////////////////////
    private boolean verificacao = false, auxA = false;
    private double variacaoXCliqueMouseComOCantoSuperiorEsquerdoVertice, ultimoInstanteX;
    private double variacaoYCliqueMouseComOCantoSuperiorEsquerdoVertice, ultimoInstanteY;
    private boolean downShift, selecionadoUm, selecioneiComShift, selecaoPadrao;
    ArrayList<State> statesSelecionados = new ArrayList<State>();

    private EventHandler<? super MouseEvent> aoIniciarArrastoVerticeComOMouse = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent e) {

            //                         HAND MOVE                                     //
            if (mModoAtual == MODO_MOVER) {
                mViewer.getNode().setCursor(Cursor.CLOSED_HAND);
                if (e.getClickCount() == 2) {
                    mViewer.getNode().setTranslateX(mViewerTranslateXPadrao);
                    mViewer.getNode().setTranslateY(mViewerTranslateYPadrao);
                }
                //gravar cordenadas x e y do mViewer de acordo com a posiÃ§Ã£o do mouse
                mouseHandX = e.getSceneX();
                mouseHandY = e.getSceneY();
                //get the x and y position measure from Left-Top
                posicaoMViewerHandX = mViewer.getNode().getTranslateX();
                posicaoMViewerHandY = mViewer.getNode().getTranslateY();
            }

            if (mModoAtual != MODO_VERTICE && mModoAtual != MODO_NENHUM) {
                return;
            }
            if (mModoAtual == MODO_NENHUM) {
                coordenadaInicialX = e.getX();
                coordenadaInicialY = e.getY();
                ultimoInstanteX = 0;
                ultimoInstanteY = 0;
                segundaVezEmDiante = false;
                //selecioneiComShift=false;


                if (e.isShiftDown()) {
                    downShift = true;

                }

                if (downShift && mComponentSobMouse != null) {

                    StateView stateView = (StateView) mComponentSobMouse;
                    State state = stateView.getState();

                    for(State s : statesSelecionados){
                        if(s==state){
                            return;
                        }
                    }
                    state.setBorderWidth(2);
                    state.setBorderColor("blue");
                    state.setTextColor("blue");
                    state.setTextSyle(State.TEXTSTYLE_BOLD);
                    statesSelecionados.add(state);
                    selecionadoUm = true;
                    selecioneiComShift = true;

                    modoCriacaoDoRetangulo = false;
                    downShift = false;
                    selecaoPadrao = false;
                    return;
                } else {

                    if (!StatesSelecionadoPeloRetangulo && mComponentSobMouse != null && !selecioneiComShift) {


                        selecaoPadrao = true;
                        if (statesSelecionados != null) {
                            statesSelecionados.clear();
                        }
                        if (mComponentSobMouse instanceof StateView) {
                            setComponenteSelecionado(mComponentSobMouse);
                            StateView stateView = (StateView) mComponentSobMouse;
                            State state = stateView.getState();
                            statesSelecionados.add(state);
                            modoCriacaoDoRetangulo = false;
                            return;
                        } else {
                            setComponenteSelecionado(mComponentSobMouse);
                        }
                    } else {
                        if (!SeClickeiEntreSelecionados(e.getX(), e.getY()) && statesSelecionados != null) {

                            verificacao = true;
                            for (State s : statesSelecionados) {
                                s.setBorderWidth(1);
                                s.setBorderColor("black");
                                s.setTextColor("black");
                                s.setTextSyle(State.TEXTSTYLE_NORMAL);
                            }
                            if (mComponentSobMouse != null) {
                                selecaoPadrao = true;
                                if (statesSelecionados != null) {
                                    statesSelecionados.clear();
                                    modoCriacaoDoRetangulo = false;
                                }

                                StateView stateView = null;
                                setComponenteSelecionado(mComponentSobMouse);
                                try {
                                    stateView = (StateView) mComponentSobMouse;
                                } catch (ClassCastException exception){
                                    return;
                                }
                                State state = stateView.getState();
                                statesSelecionados.add(state);


                            } else {
                                selecaoPadrao = false;
                                modoCriacaoDoRetangulo = true;
                                StatesSelecionadoPeloRetangulo = false;
                                selecioneiComShift = false;
                                if (selecionadoUm) {
                                    selecionadoUm = false;
                                }
                                for (State s : statesSelecionados) {
                                    s.setBorderWidth(1);
                                    s.setBorderColor("black");
                                    s.setTextColor("black");
                                    s.setTextSyle(State.TEXTSTYLE_NORMAL);
                                }

                                statesSelecionados.clear();
                                //System.out.println("Removendo estilo selecionado state/trans");
                                Object v = getSelectedView();
                                if(v instanceof TransitionView){
                                    removeSelectedStyles(v);
                                }

                            }

                        } else {
                            verificacao = false;
                            modoCriacaoDoRetangulo = false;
                        }

                    }
                }

                if (!(mComponentSobMouse instanceof StateView)) {

                    return;
                }

                StateView v = (StateView) mComponentSobMouse;

                variacaoXCliqueMouseComOCantoSuperiorEsquerdoVertice = v.getNode().getLayoutX() - e.getX() + RAIO_CIRCULO;
                variacaoYCliqueMouseComOCantoSuperiorEsquerdoVertice = v.getNode().getLayoutY() - e.getY() + RAIO_CIRCULO;
            }
        }
    };


    private double variacaoX, variacaoY;
    private boolean segundaVezEmDiante;
    private double largura = 0, altura = 0;
    private double inicioDoRectanguloX, inicioDoRectanguloY, inicioDoRectanguloXAux, inicioDoRectanguloYAux;
    //////////////////////////////////////////////////////////////////////////////////////////
    // clickar e nÃƒÂ£o soltar e mover o mouse(precionando e movendo/dragg)
    //////////////////////////////////////////////////////////////////////////////////////////
    private EventHandler<? super MouseEvent> aoArrastarVerticeComOMouse = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent t) {

            //                         HAND MOVE                                     //
            if (mModoAtual == MODO_MOVER) {
                //Pegar Moviemnto do mouse
                posicaoMViewerHandX += t.getSceneX() - mouseHandX;
                posicaoMViewerHandY += t.getSceneY() - mouseHandY;
                //setar nova posição apos calculo do movimento
                mViewer.getNode().setTranslateX(posicaoMViewerHandX);
                mViewer.getNode().setTranslateY(posicaoMViewerHandY);
                //setar nova posição do mouse no mViewer
                mouseHandX = t.getSceneX();
                mouseHandY = t.getSceneY();
            }

            if (mModoAtual != MODO_VERTICE && mModoAtual != MODO_NENHUM) {
                return;
            }
            if (mModoAtual == MODO_NENHUM) {
                if ((StatesSelecionadoPeloRetangulo || selecionadoUm ) && !selecaoPadrao) {
//                        if(selecioneiComShift){
//                            System.out.println("retornou?");
//                            //selecioneiComShift=false;
//                            return;
//                        }

                    if (!segundaVezEmDiante //&& downShift
                            ) {
                        //System.out.println("primeira vez");
                        variacaoX = t.getX() - coordenadaInicialX;
                        variacaoY = t.getY() - coordenadaInicialY;
                        segundaVezEmDiante = true;
                        ultimoInstanteX = t.getX();
                        ultimoInstanteY = t.getY();

                    } else {
                        //System.out.println("seunda vez");
                        variacaoX = (t.getX() - ultimoInstanteX);
                        variacaoY = (t.getY() - ultimoInstanteY);
                        ultimoInstanteX = t.getX();
                        ultimoInstanteY = t.getY();

                    }

                    for (State s : statesSelecionados) {
                        posicionandoConjuntoDeStates(s, variacaoX, variacaoY);
                    }

                } else {

                    if (modoCriacaoDoRetangulo) {
                        //System.out.println("entra aqui 1");

                        auxA = true;

                        coornenadaIstanteX = t.getX();
                        coordenadaIstanteY = t.getY();


                        if (coornenadaIstanteX <= coordenadaInicialX) {

                            if (coordenadaIstanteY <= coordenadaInicialY) {
                                largura = coordenadaInicialY - coordenadaIstanteY;
                                altura = coordenadaInicialX - coornenadaIstanteX;
                                inicioDoRectanguloX = coornenadaIstanteX;
                                inicioDoRectanguloY = coordenadaIstanteY;
                                inicioDoRectanguloXAux = coordenadaInicialX;
                                inicioDoRectanguloYAux = coordenadaInicialY;
                            }
                            if (coordenadaIstanteY >= coordenadaInicialY) {
                                altura = coordenadaInicialX - coornenadaIstanteX;
                                largura = coordenadaIstanteY - coordenadaInicialY;
                                inicioDoRectanguloX = coordenadaInicialX - altura;
                                inicioDoRectanguloY = coordenadaInicialY;
                                inicioDoRectanguloXAux = coordenadaInicialX;
                                inicioDoRectanguloYAux = coordenadaInicialY;
                            }

                        } else {
                            if (coordenadaIstanteY <= coordenadaInicialY) {
                                altura = coornenadaIstanteX - coordenadaInicialX;
                                largura = coordenadaInicialY - coordenadaIstanteY;
                                inicioDoRectanguloX = coordenadaInicialX;
                                inicioDoRectanguloY = coordenadaInicialY - largura;
                                inicioDoRectanguloXAux = coordenadaInicialX;
                                inicioDoRectanguloYAux = coordenadaInicialY;
                            }
                            if (coordenadaIstanteY >= coordenadaInicialY) {
                                altura = coornenadaIstanteX - coordenadaInicialX;
                                largura = coordenadaIstanteY - coordenadaInicialY;
                                inicioDoRectanguloX = coordenadaInicialX;
                                inicioDoRectanguloY = coordenadaInicialY;
                                inicioDoRectanguloXAux = coordenadaInicialX;
                                inicioDoRectanguloYAux = coordenadaInicialY;
                            }

                        }
                        if (ultimoRetanguloAdicionado != null) {
                            mViewer.getNode().getChildren().remove(ultimoRetanguloAdicionado);
                        }

                        Rectangle retangulo = new Rectangle((int) inicioDoRectanguloX, (int) inicioDoRectanguloY, (int) altura, (int) largura);
                        ultimoRetanguloAdicionado = retangulo;
                        retangulo.setFill(Color.BLUE);
                        retangulo.setOpacity(0.4);
                        retangulo.setVisible(true);
                        mViewer.getNode().getChildren().add(retangulo);

                    } else {

                        if (!(mComponentSobMouse instanceof StateView) || !selecaoPadrao) {
                            return;
                        }
                        //System.out.println("entra aqui 2");

                        State s = ((StateView) mComponentSobMouse).getState();
                        s.setLayoutX(t.getX() + variacaoXCliqueMouseComOCantoSuperiorEsquerdoVertice - RAIO_CIRCULO);
                        s.setLayoutY(t.getY() + variacaoYCliqueMouseComOCantoSuperiorEsquerdoVertice - RAIO_CIRCULO);
                    }
                }
            }
        }
    };
    ////////////////////////////////////////////////////////////////////
    //Soltar
    ///////////////////////////////////////////////////////////////////
    private EventHandler<? super MouseEvent> aoLiberarVerticeArrastadoComOMouse = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent t) {

            if (mModoAtual == MODO_MOVER) {
                mViewer.getNode().setCursor(Cursor.OPEN_HAND);
            }

            if (mModoAtual != MODO_VERTICE && mModoAtual != MODO_NENHUM) {
                return;
            }
            if (mModoAtual == MODO_NENHUM) {
                variacaoXCliqueMouseComOCantoSuperiorEsquerdoVertice = 0;
                variacaoYCliqueMouseComOCantoSuperiorEsquerdoVertice = 0;
                if (modoCriacaoDoRetangulo) {
                    if (!auxA) {
                        inicioDoRectanguloXAux = coordenadaInicialX;
                        inicioDoRectanguloYAux = coordenadaInicialY;
                    }
                    if (!selecionadoUm) {
                        coordenadaFinalX = t.getX();
                        coordenadaFinalY = t.getY();

                        StatesSelecionadoPeloRetangulo = selecionandoComRetangulo(inicioDoRectanguloXAux, inicioDoRectanguloYAux, coordenadaFinalX, coordenadaFinalY);
                        if(StatesSelecionadoPeloRetangulo){
                            System.out.println("chegou aqui correto");
                            paleta.setVisible(true);
                        }
                    }
                    if (ultimoRetanguloAdicionado != null) {
                        mViewer.getNode().getChildren().remove(ultimoRetanguloAdicionado);
                    }
                    auxA = false;
                }
            }
        }
    };
    private ArrayList<State> todosOsStates;

    public boolean selecionandoComRetangulo(double inicioDoRectanguloX, double inicioDoRectanguloY, double finalDoRectanguloX, double finalDoRectanguloY) {
        boolean aux = false;
        /////////////////////////////////////////////////
        ////////ORGANIZANDO AS POSICOES DO RETANGULO DE SELECAO
        /////////////////////////////////////////////////
        if (inicioDoRectanguloX > finalDoRectanguloX) {
            double ajuda = finalDoRectanguloX;
            finalDoRectanguloX = inicioDoRectanguloX;
            inicioDoRectanguloX = ajuda;

        }
        if (inicioDoRectanguloY > finalDoRectanguloY) {
            double ajuda = finalDoRectanguloY;
            finalDoRectanguloY = inicioDoRectanguloY;
            inicioDoRectanguloY = ajuda;


        }

        todosOsStates = (ArrayList<State>) mViewer.getComponent().getStates();
        int n = todosOsStates.size();
        if (stateDentroDoRetangulo != null) {
            for (State s : stateDentroDoRetangulo) {
                statesSelecionados.remove(s);
            }
            stateDentroDoRetangulo.clear();
        }

        for (int i = 0; i < n; i++) {
            State s = todosOsStates.get(i);
            posCircleX = s.getLayoutX() + RAIO_CIRCULO;
            posCircleY = s.getLayoutY() + RAIO_CIRCULO;

            posicaoDoEstadoXMaisRaio = posCircleX + RAIO_CIRCULO;
            posicaoDoEstadoYMaisRaio = posCircleY + RAIO_CIRCULO;
            posicaoDoEstadoXMenosRaio = posCircleX - RAIO_CIRCULO;
            posicaoDoEstadoYMenosRaio = posCircleY - RAIO_CIRCULO;

            //verificando se a area do retangulo estÃ¡ pegando atÃ© o centro do state
            if (posicaoDoEstadoXMenosRaio == inicioDoRectanguloX || posicaoDoEstadoXMenosRaio > inicioDoRectanguloX) {
                if (posicaoDoEstadoXMaisRaio == finalDoRectanguloX || posicaoDoEstadoXMaisRaio < finalDoRectanguloX) {
                    if (posicaoDoEstadoYMenosRaio == inicioDoRectanguloY || posicaoDoEstadoYMenosRaio > inicioDoRectanguloY) {
                        if (posicaoDoEstadoYMaisRaio == finalDoRectanguloY || posicaoDoEstadoYMaisRaio < finalDoRectanguloY) {
                            stateDentroDoRetangulo.add(s);
                            s.setBorderWidth(2);
                            s.setBorderColor("blue");
                            s.setTextColor("blue");
                            s.setTextSyle(State.TEXTSTYLE_BOLD);

                            aux = true;
                        } else {
                            s.setBorderWidth(1);
                            s.setBorderColor("black");
                            s.setTextColor("black");
                            s.setTextSyle(State.TEXTSTYLE_NORMAL);
                        }
                    } else {
                        s.setBorderWidth(1);
                        s.setBorderColor("black");
                        s.setTextColor("black");
                        s.setTextSyle(State.TEXTSTYLE_NORMAL);
                    }
                } else {
                    s.setBorderWidth(1);
                    s.setBorderColor("black");
                    s.setTextColor("black");
                    s.setTextSyle(State.TEXTSTYLE_NORMAL);
                }
            } else {
                s.setBorderWidth(1);
                s.setBorderColor("black");
                s.setTextColor("black");
                s.setTextSyle(State.TEXTSTYLE_NORMAL);
            }
        }
        statesSelecionados.addAll(stateDentroDoRetangulo);

        //MUDANDO ICONE E SELECAO DO TOGGLEBUTON BIGSTATE
        changeIconToggleBigState();




        return aux;
    }

    public boolean SeClickeiEntreSelecionados(double x, double y) {
        boolean aux = false;
        if (statesSelecionados != null) {

            for (State s : statesSelecionados) {
                posCircleX = s.getLayoutX() + RAIO_CIRCULO;
                posCircleY = s.getLayoutY() + RAIO_CIRCULO;

                posicaoDoEstadoXMaisRaio = posCircleX + RAIO_CIRCULO;
                posicaoDoEstadoYMaisRaio = posCircleY + RAIO_CIRCULO;
                posicaoDoEstadoXMenosRaio = posCircleX - RAIO_CIRCULO;
                posicaoDoEstadoYMenosRaio = posCircleY - RAIO_CIRCULO;

                if (x == posicaoDoEstadoXMenosRaio || x > posicaoDoEstadoXMenosRaio) {
                    if (x == posicaoDoEstadoXMaisRaio || x < posicaoDoEstadoXMaisRaio) {
                        if (y == posicaoDoEstadoYMenosRaio || y > posicaoDoEstadoYMenosRaio) {
                            if (y == posicaoDoEstadoYMaisRaio || y < posicaoDoEstadoYMaisRaio) {
                                return true;
                            }

                        }
                    }
                }
            }
        }

        return aux;
    }


    ////////////////////////////////////////////////////////////////////////////
    // Adicionar transiÃ§Ã£o
    ////////////////////////////////////////////////////////////////////////////
    private StateView mVerticeOrigemParaAdicionarTransicao;
    private StateView mVerticeDestinoParaAdicionarTransicao;
    private Line ultimaLinha;
    private double xInicial,yInicial;
    private EventHandler<MouseEvent> aoDetectarDragSobreVertice = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent t) {
            //System.out.println("EVENTO DE CLICK");
            if (mModoAtual != MODO_TRANSICAO) {
                return;
            }

            if (!(mComponentSobMouse instanceof StateView)) {
                return;
            }
            StateView v = (StateView) mComponentSobMouse;

            ///pegando posicioes inicias (x,y)
            xInicial=v.getState().getLayoutX();
            yInicial=v.getState().getLayoutY();

            //guarda o objeto no qual iniciamos o drag            
            mVerticeOrigemParaAdicionarTransicao = v;

            if (BigState.verifyIsBigState(mVerticeOrigemParaAdicionarTransicao.getState())) {
                JOptionPane.showMessageDialog(null, "Impossible to create transitions in a Big State!", "Alert", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if(mVerticeOrigemParaAdicionarTransicao.getState().isFinal()){
                JOptionPane.showMessageDialog(null, "Impossible to create transitions in a Final State!", "Alert", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if(mVerticeOrigemParaAdicionarTransicao.getState().isError()){
                JOptionPane.showMessageDialog(null, "Impossible to create transitions in a Error State!", "Alert", JOptionPane.WARNING_MESSAGE);
                return;
            }

            //inicia o drag'n'drop
            Dragboard db = mVerticeOrigemParaAdicionarTransicao.getNode().startDragAndDrop(TransferMode.ANY);

            //soh funciona com as trÃªs linhas a seguir. Porque? Eu nÃ£o sei.
            ClipboardContent content = new ClipboardContent();
            content.putString("gambiarra");
            db.setContent(content);

            //indica que este evento foi realizado
            t.consume();
        }
    };
    private final double AJUSTE_X=20,AJUSTE_y=20;
    private EventHandler<DragEvent> aoDetectarPossivelAlvoParaSoltarODrag = new EventHandler<DragEvent>() {
        @Override
        public void handle(DragEvent event) {
            double xFinal=event.getX(),yFinal=event.getY();
            //System.out.println("EVENTO DE PUXAR");
            //a informaÃ§ao esta sendo solta sobre o alvo
            //aceita soltar o mouse somente se nÃ£o Ã© o mesmo nodo de origem 
            //e possui uma string            
            if (event.getGestureSource() != event.getSource()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }

            Object v = getComponentePelaPosicaoMouse(new Point2D(event.getSceneX(), event.getSceneY()));
            mVerticeDestinoParaAdicionarTransicao = (v instanceof StateView) ? ((StateView) v) : null;
            /*<><><><><><><><><><><><><><><><><><><><><><<><><><><><><><><><><><><><><><><><><><><><><><><><>*/
            if(ultimaLinha!=null){
                mViewer.getNode().getChildren().remove(ultimaLinha);
            }

            Line linha= createViewFakeTransitionLine(xInicial, yInicial, xFinal, yFinal);
            mViewer.getNode().getChildren().add(linha);
            linha.toBack();///<-coloca a linha por trás do state
            //System.out.println("ADICIONOU");
            ultimaLinha=linha;
            
            /*<><><><><><><><><><><><><><><><><><><><><><<><><><><><><><><><><><><><><><><><><><><><><><><><>*/
            event.consume();
        }
    };

    private Line createViewFakeTransitionLine(double xInicial, double yInicial, double xFinal, double yFinal) {
        Line linha = new Line();
        linha.setStartX(xInicial+AJUSTE_X);
        linha.setStartY(yInicial+AJUSTE_y);
        linha.setEndX(xFinal);
        linha.setEndY(yFinal);
           /* System.out.println(event.getX());
            System.out.println(event.getY());*/
        linha.setOpacity(0.5);
        linha.getStrokeDashArray().addAll(2d);//<-traseja o state
        return linha;
    }

    private final EventHandler<DragEvent> aoSoltarMouseSobreVertice = new EventHandler<DragEvent>() {
        @Override
        public void handle(DragEvent event) {
            //System.out.println("EVENTO DE SOLTA");
            if (mModoAtual != MODO_TRANSICAO) {
                return;
            }
            /*<><><><><><><><><><><><><><><><><><><><><><<><><><><><><><><><><><><><><><><><><><><><><><><><>*/
            if(ultimaLinha!=null){
                mViewer.getNode().getChildren().remove(ultimaLinha);
                //System.out.println("REMOVEU FINAL");
            }
            /*<><><><><><><><><><><><><><><><><><><><><><<><><><><><><><><><><><><><><><><><><><><><><><><><>*/

            if (mVerticeDestinoParaAdicionarTransicao != null) {
                if (BigState.verifyIsBigState(mVerticeDestinoParaAdicionarTransicao.getState())) {
                    JOptionPane.showMessageDialog(null, "Impossible to create transitions for a BigState!", "Alert", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                State o = mVerticeOrigemParaAdicionarTransicao.getState();
                State d = mVerticeDestinoParaAdicionarTransicao.getState();
                mExibirPropriedadesTransicao = true;

                int qtdeTransitionOD = o.getTransitionsTo(d).size();
                int qtdeTransitionDO = d.getTransitionsTo(o).size();
                List<Transition> transitionsOD = o.getTransitionsTo(d);
                List<Transition> transitionsDO = d.getTransitionsTo(o);
                boolean temLineOD = verificarSeExisteTransitionLine(transitionsOD);
                boolean temLineDO = verificarSeExisteTransitionLine(transitionsDO);

                if(mTransitionViewType == 1){ // curve
                    Transition t = mViewer.getComponent().buildTransition(o, d)
                            .setViewType(mTransitionViewType)
                            .create();
                    applyDefaults(t);
                }
                //line  com auto ajuste
                else if(qtdeTransitionOD == 0 && qtdeTransitionDO == 0){
                    Transition t = mViewer.getComponent().buildTransition(o, d)
                            .setViewType(mTransitionViewType)
                            .create();
                    applyDefaults(t);
                }
                else if(qtdeTransitionOD == 0 && qtdeTransitionDO > 0 && !temLineDO){
                    Transition t = mViewer.getComponent().buildTransition(o, d)
                            .setViewType(TransitionView.Geometry.LINE)
                            .create();
                    applyDefaults(t);
                }
                else if(qtdeTransitionOD == 0 && qtdeTransitionDO > 0 && temLineDO){
                    Transition t = mViewer.getComponent().buildTransition(o, d)
                            .setViewType(TransitionView.Geometry.CURVE)
                            .create();
                    applyDefaults(t);
                }
                else if(qtdeTransitionOD > 0 && !temLineOD && qtdeTransitionDO == 0){
                    Transition t = mViewer.getComponent().buildTransition(o, d)
                            .setViewType(TransitionView.Geometry.LINE)
                            .create();
                    applyDefaults(t);
                }
                else if(qtdeTransitionOD > 0 && temLineOD && qtdeTransitionDO == 0){
                    Transition t = mViewer.getComponent().buildTransition(o, d)
                            .setViewType(TransitionView.Geometry.CURVE)
                            .create();
                    applyDefaults(t);
                }
                else if(qtdeTransitionOD > 0 && !temLineOD && qtdeTransitionDO > 0 && !temLineDO){
                    Transition t = mViewer.getComponent().buildTransition(o, d)
                            .setViewType(TransitionView.Geometry.LINE)
                            .create();
                    applyDefaults(t);
                }
                else if(qtdeTransitionOD > 0 && temLineOD && qtdeTransitionDO > 0 && !temLineDO){
                    Transition t = mViewer.getComponent().buildTransition(o, d)
                            .setViewType(TransitionView.Geometry.CURVE)
                            .create();
                    applyDefaults(t);
                }
                else if(qtdeTransitionOD > 0 && !temLineOD &&qtdeTransitionDO > 0 && temLineDO){
                    Transition t = mViewer.getComponent().buildTransition(o, d)
                            .setViewType(TransitionView.Geometry.CURVE)
                            .create();
                    applyDefaults(t);
                }
            }

            event.setDropCompleted(true);
            event.consume();
        }

        private boolean verificarSeExisteTransitionLine(List<Transition> transitions){
            boolean line = false;
            for(Transition t : transitions){
                if((int)t.getValue("view.type") == 0){
                    line = true;
                }
            }
            return line;
        }

        private void applyDefaults(Transition t) {
            if (mDefaultTransitionLabel != null) {
                t.setLabel(mDefaultTransitionLabel);
            }
            if (mDefaultTransitionColor != null) {
                t.setColor(mDefaultTransitionColor);
            }
            if (mDefaultTransitionTextColor != null) {
                t.setTextColor(mDefaultTransitionTextColor);
            }
            if (mDefaultTransitionWidth != null) {
                t.setWidth(mDefaultTransitionWidth);
            }
        }

    };
///////////////////////////////////////////////////////////////////////////////
//                       TECLAS PRECIONADAS                             //////
/////////////////////////////////////////////////////////////////////////////    

    private EventHandler<KeyEvent> teclaPressionada = new EventHandler<KeyEvent>() {

        @Override
        public void handle(KeyEvent event) {
            if(event.getCode().equals(KeyCode.DELETE)){
                System.out.println("entrou no delete");
                if (mComponentSobMouse instanceof StateView) {
                    State v = ((StateView) mComponentSobMouse).getState();
                    if(v.getValue("bigstate") instanceof BigState){
                        BigState.removeBigState((BigState) v.getValue("bigstate"));
                    }
                    mViewer.getComponent().remove(v);
                } else if (mComponentSobMouse instanceof TransitionView) {
                    Transition t = ((TransitionView) mComponentSobMouse).getTransition();
                    State iniTransition = t.getSource();
                    State fimTransition = t.getDestiny();
                    mViewer.getComponent().remove(t);
                    //Verificar Mais de uma Trasition do mesmo Source e Destiny
                    List<Transition> multiplasTransicoes = iniTransition.getTransitionsTo(fimTransition);
                    if(multiplasTransicoes.size() > 0){
                        //deletar da tela
                        for(Transition trans : multiplasTransicoes){
                            mViewer.getComponent().remove(trans);
                        }
                        //recriar transitions
                        for(Transition trans : multiplasTransicoes){
                            mViewer.getComponent().buildTransition(iniTransition, fimTransition)
                                    .setGuard(trans.getGuard())
                                    .setLabel(trans.getLabel())
                                    .setProbability(trans.getProbability())
                                    .setViewType(TransitionView.Geometry.CURVE)
                                    .create();
                        }
                    }
                }
            }
            /*else if(event.getCode() ==  new KeyCombination(KeyCode.Z,KeyCombination.CONTROL_DOWN)){
                
            }
            else if(event.getCode() == KeyCode.DELETE){
                
            }*/
        }
    };
    ///////////////////////////////////////////////////////////////////////////////
//                             ZOOM                                     //////
/////////////////////////////////////////////////////////////////////////////
    private EventHandler<? super ScrollEvent> zoom = new EventHandler<ScrollEvent>() {

        @Override
        public void handle(ScrollEvent event) {
            if (event.isControlDown()) {
                zoomReset.setSelected(false);
                final double SCALE_DELTA = 1.1;
                double scaleFactor = (event.getDeltaY() > 0) ? SCALE_DELTA : 1 / SCALE_DELTA;
                zoom(mViewer.getNode(), event.getX(), event.getY(), scaleFactor);
            }
        }
    };

    private void zoom(Node node, double centerX, double centerY, double factor) {
        final Point2D center = node.localToParent(centerX, centerY);
        final Bounds bounds = node.getBoundsInParent();
        final double w = bounds.getWidth();
        final double h = bounds.getHeight();

        final double dw = w * (factor - 1);
        final double xr = 2 * (w / 2 - (center.getX() - bounds.getMinX())) / w;

        final double dh = h * (factor - 1);
        final double yr = 2 * (h / 2 - (center.getY() - bounds.getMinY())) / h;

        node.setScaleX(node.getScaleX() * factor);
        node.setScaleY(node.getScaleY() * factor);
        node.setTranslateX(node.getTranslateX() + xr * dw / 2);
        node.setTranslateY(node.getTranslateY() + yr * dh / 2);
    }

    private void changeColorsState(ColorPicker cores, String tipo){
        if(statesSelecionados==null){
            return;
        }
        String hexCor = "";
        if(cores.getValue().toString().equals("0x000000ff")){
            hexCor = "black";
        }else{
            hexCor = "#"+ Integer.toHexString(cores.getValue().hashCode()).substring(0, 6).toUpperCase();
        }
        for(State s : statesSelecionados){
            if(s.isInitial() || s.isFinal() || s.isError()){
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "-Initial\n-Final\n-Error", ButtonType.OK);
                alert.setHeaderText("Impossible to change color of States:");
                alert.show();
                return;
            }
            if(tipo.equals("Default")){
                s.setColor(null);
            }
            if(tipo.equals("MultiSelecao")){
                s.setColor(hexCor);
            }
        }
    }

    @Override
    public Component getComponent() {
        return mViewer.getComponent();
    }

    @Override
    public void setComponent(Component component) {
        mViewer.setComponent(component);
    }

    public void setModo(int modo) {
        this.mModoAtual = modo;
        mViewer.getNode().setCursor(Cursor.DEFAULT);
        mStateToolbar.setVisible(mModoAtual == MODO_VERTICE);
        mTransitionToolbar.setVisible(mModoAtual == MODO_TRANSICAO);
        if (mModoAtual == MODO_MOVER) {
            mViewer.getNode().setCursor(Cursor.OPEN_HAND);
        }
    }

    private Object getComponentePelaPosicaoMouse(Point2D point) {
        Object v = mViewer.locateStateView(point);
        if (v == null) {
            v = mViewer.locateTransitionView(point);
        }
        return v;
    }

    private void setComponenteSelecionado(Object t) {
        if (mComponentSelecionado != null) {
            removeSelectedStyles(mComponentSelecionado);
        }
        mComponentSelecionado = t;
        if (t != null) {
            updatePropriedades(t);
            applySelectedStyles(mComponentSelecionado);
        }
        //System.out.println("chegou aqui ");

//        for (Listener l : mListeners) {
//            l.onSelectionChange(this);
//        }
    }

    private void updatePropriedades(Object t) {
        if (t instanceof TransitionView) {
            Transition tt = ((TransitionView) t).getTransition();
            txtGuard.setText(tt.getGuard());
            txtProbability.setText(tt.getProbability() == null ? null : String.valueOf(tt.getProbability()));
            txtLabel.setText(tt.getLabel());
            txtLabel.requestFocus();
        }
    }

    public Object getSelectedView() {
        return mComponentSelecionado;
    }

    private void applySelectedStyles(Object v) {
        //System.out.println("applyselectedstyles " + v);
        if (v instanceof StateView) {
            State s = ((StateView) v).getState();
            s.setBorderWidth(2);
            s.setBorderColor("blue");
            s.setTextColor("blue");
            s.setTextSyle(State.TEXTSTYLE_BOLD);
        } else if (v instanceof TransitionView) {
            Transition t = ((TransitionView) v).getTransition();
            t.setWidth(1);
            t.setColor("blue");
            t.setTextColor("blue");
            t.setTextSyle(State.TEXTSTYLE_BOLD);
        }
    }

    private void removeSelectedStyles(Object v) {
        // System.out.println("removeselectedstyles " + v);
        if (v instanceof StateView) {
            State s = ((StateView) v).getState();
            if (s == null){
                return;
            }
            s.setBorderWidth(1);
            s.setBorderColor("black");
            s.setTextColor("black");
            s.setTextSyle(State.TEXTSTYLE_NORMAL);
        } else if (v instanceof TransitionView) {
            Transition t = ((TransitionView) v).getTransition();
            if (t == null) {
                return;
            }
            t.setWidth(1);
            t.setColor("black");
            t.setTextColor("black");
            t.setTextSyle(State.TEXTSTYLE_NORMAL);
        }
    }

    double posXAntes;
    double posYAntes;

    public void posicionandoConjuntoDeStates(State v, double mX, double mY) {
        double posX = v.getLayoutX();
        double posY = v.getLayoutY();
        posXAntes = posX;
        posYAntes = posY;
        double distanciaClick;
        double distanciaState;
        double deltaX, deltaY;
//        mX = mX - RAIO_CIRCULO;
//        mY = mY - RAIO_CIRCULO;
        //System.out.println("varuacaoX-raio" + mX);
        //System.out.println("varuacaoY-raio" + mY);
//        System.out.println("mX" + mX);
//        System.out.println("mY" + mY);
//        System.out.println("posX" + posX);
//        System.out.println("posY" + posY);
//        distanciaClick = Math.floor(Math.sqrt((mX * mX) + (mY * mY)));
//        distanciaState = Math.floor(Math.sqrt((posX * posX) + (posY * posY)));
//        System.out.println("distanciaClick" + distanciaClick);
//        System.out.println("distanciaState" + distanciaState);
//        if (distanciaClick > distanciaState) {
//            deltaX = mX - posX;
//            deltaY = mY - posY;
//            v.setLayoutX(posX + deltaX);
//            v.setLayoutY(posY + deltaY);
//
//        }
//        if (distanciaClick < distanciaState) {
//            deltaX = posX - mX;
//            deltaY = posY - mY;
//            v.setLayoutX(posX + deltaX);
//            v.setLayoutY(posY + deltaY);
//
//        }

        v.setLayoutX(v.getLayoutX() + mX);
        v.setLayoutY(v.getLayoutY() + mY);
        //System.out.println(v.getLayoutX());
        //System.out.println(v.getLayoutY());
    }

    public void addListener(Listener l) {
        mListeners.add(l);
    }

    public void removeListener(Listener l) {
        mListeners.remove(l);
    }

    private void updateContID() {
        int aux = -1;
        for (State s : mViewer.getComponent().getStates()) {
            if (s.getID() > aux) {
                aux = s.getID();
            }
        }
        contID = aux;
        contID++;
    }

    //Historico para Desfazer e Refazer
    private void historicoViewer(String opcao) {
        switch(opcao){
            case "Desfazer":{
                if(contPosHistoricoCheia>0){
                    mScrollPanel.getChildrenUnmodifiable().clear();
                    mScrollPanel.getChildrenUnmodifiable().add(mUndoRedo[contPosHistoricoCheia-1].getNode());
                }
            }break;

            case "Refazer":{

            }break;
        }
    }
    private int contPosHistoricoCheia = 0;
    private void addHistorico(ComponentView viewer){
        if(contPosHistoricoCheia <= tamHistorico){
            mUndoRedo[contPosHistoricoCheia] = viewer;
            contPosHistoricoCheia+=1;
        }else{
            for(int i=1;i<=tamHistorico;i++){
                mUndoRedo[i-1] = mUndoRedo[i];
            }
            contPosHistoricoCheia-=1;
            mUndoRedo[contPosHistoricoCheia] = viewer;
            contPosHistoricoCheia+=1;
        }
    }

    private void changeIconToggleBigState(){
        if(statesSelecionados.size()==1){
            BigState bigState = (BigState) statesSelecionados.get(0).getValue("bigstate");
            if (bigState != null){
                mBtnBigState.setSelected(true);
                mBtnBigState.setGraphic(iconBigStateDismount);
            }
            else {
                mBtnBigState.setSelected(false);
                mBtnBigState.setGraphic(iconBigState);
            }
        }
        else {
            mBtnBigState.setSelected(false);
            mBtnBigState.setGraphic(iconBigState);
        }
    }

}