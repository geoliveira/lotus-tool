/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uece.lotus.uml.app.project;

import br.uece.lotus.Component;
import br.uece.lotus.State;
import br.uece.lotus.Transition;
import br.uece.lotus.project.ProjectExplorer;
import br.uece.lotus.uml.api.ds.BlockDS;
import br.uece.lotus.uml.api.ds.Hmsc;
import br.uece.lotus.uml.api.ds.StandardModeling;
import br.uece.lotus.uml.api.ds.ComponentDS;
import br.uece.lotus.uml.api.ds.ProjectDS;
import br.uece.lotus.uml.api.ds.TransitionMSC;
import br.uece.lotus.uml.api.project.ProjectExplorerDS;
import br.uece.lotus.uml.designer.blockDiagramModeling.DesingWindowImplManegerBlockDs;
import br.uece.lotus.uml.designer.standardModeling.StandardModelingWindowManager;
import br.uece.lotus.uml.designer.windowLTS.LtsWindowManager;
import br.uece.seed.app.ExtensibleFXContextMenu;
import br.uece.seed.app.ExtensibleMenu;
import br.uece.seed.app.ExtensibleTabPane;
import br.uece.seed.app.UserInterface;
import br.uece.seed.ext.ExtensionManager;
import br.uece.seed.ext.Plugin;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
//import sun.reflect.generics.tree.Tree;

/**
 *
 * @author Bruno Barbosa
 */
public final class ProjectExplorerPluginDS extends Plugin implements ProjectExplorerDS{

    private UserInterface mUserInterface;
    
    private final ContextMenu mMnuWorkspace;
    private final ContextMenu mMnuProjectDS;
    private final ContextMenu mMnuStandardModeling;
    private final ContextMenu mMnuComponentBMSC;
    private final ContextMenu mMnuComponentLTS;
    
    private final ExtensibleMenu mExtMnuWorkspace;
    private final ExtensibleMenu mExtMnuProjectDS;
    private final ExtensibleMenu mExtMnuStandardModeling;
    private final ExtensibleMenu mExtMnuBMSC;
    private final ExtensibleMenu mExtMnuComponentLTS;
    
    private final TreeView<WrapperDS> mProjectDSView;
    
    private final List<Listener> mListeners;
    
    private final ProjectDS.Listener mProjectDSListener = new ProjectDS.Listener() {

        @Override
        public void onChange(ProjectDS project) {
            TreeItem<WrapperDS> itm = findItem(mProjectDSView.getRoot(), project);
            WrapperDS w = itm.getValue();
            itm.setValue(null);
            itm.setValue(w);
        }

        @Override
        public void onComponentBMSCCreated(ProjectDS project, ComponentDS componentDs) {
            TreeItem<WrapperDS> raiz = findItem(mProjectDSView.getRoot(), project);
            TreeItem<WrapperDS> listaDeDiagramas = findItem(raiz, "bMSCs");
            if(listaDeDiagramas != null){
                TreeItem<WrapperDS> cds = new TreeItem<>(new WrapperDS(componentDs), new ImageView(
                                new Image(getClass().getResourceAsStream("/imagens/project/ic_component_ds.png"))));
                listaDeDiagramas.getChildren().add(cds);
            }else{
                TreeItem<WrapperDS> pastaDS = new TreeItem<>(new WrapperDS("bMSCs"), new ImageView(
                                new Image(getClass().getResourceAsStream("/imagens/project/ic_folders.png"))));
                TreeItem<WrapperDS> cds = new TreeItem<>(new WrapperDS(componentDs), new ImageView(
                                new Image(getClass().getResourceAsStream("/imagens/project/ic_component_ds.png"))));
                pastaDS.getChildren().add(cds);
                raiz.getChildren().add(pastaDS);
                findItem(raiz, "bMSCs").setExpanded(true);
            }
        }

        @Override
        public void onComponentBMSCRemoved(ProjectDS project, ComponentDS componentDs) {
            TreeItem<WrapperDS> raiz = findItem(mProjectDSView.getRoot(), project);
            TreeItem<WrapperDS> listaDeDiagramas = findItem(raiz, "bMSCs");
            TreeItem<WrapperDS> cDS = findItem(listaDeDiagramas, componentDs);
            listaDeDiagramas.getChildren().remove(cDS);
        }

        @Override
        public void onComponentLTSCreated(ProjectDS project, Component component) {
            TreeItem<WrapperDS> raiz = findItem(mProjectDSView.getRoot(), project);
            TreeItem<WrapperDS> listaDeFragmentos = findItem(raiz, "Fragments LTS");
            if(listaDeFragmentos != null){
                listaDeFragmentos.getChildren().add(new TreeItem<>(new WrapperDS(component), new ImageView(
                                new Image(getClass().getResourceAsStream("/imagens/project/ic_component_fragment.png")))));
            }else{
                TreeItem<WrapperDS> fragment = new TreeItem<>(new WrapperDS("Fragments LTS"), new ImageView(
                                new Image(getClass().getResourceAsStream("/imagens/project/ic_folders.png"))));
                TreeItem<WrapperDS> c = new TreeItem<>(new WrapperDS(component), new ImageView(
                                new Image(getClass().getResourceAsStream("/imagens/project/ic_component_fragment.png"))));
                fragment.getChildren().add(c);
                raiz.getChildren().add(fragment);
                findItem(raiz, "Fragments LTS").setExpanded(true);
            }
        }

        @Override
        public void onComponentLTSRemoved(ProjectDS project, Component component) {
            TreeItem<WrapperDS> raiz = findItem(mProjectDSView.getRoot(), project);
            TreeItem<WrapperDS> listaDeFragmentos = findItem(raiz, "Fragments LTS");
            TreeItem<WrapperDS> c = findItem(listaDeFragmentos, component);
            listaDeFragmentos.getChildren().remove(c);
        }

        @Override
        public void onComponentLTSGeralCreated(ProjectDS projectDS, StandardModeling buildDS, Component component) {
            TreeItem<WrapperDS> raiz = findItem(mProjectDSView.getRoot(), projectDS);
            TreeItem<WrapperDS> build = findItem(raiz, buildDS);
            component.setName("LTS_Composed("+projectDS.getName()+")");
            if(build.getChildren().size()>= 1){
                build.getChildren().clear();
            }
            build.getChildren().add(new TreeItem<>(new WrapperDS(component), new ImageView(
                                new Image(getClass().getResourceAsStream("/imagens/project/ic_componet_lts_geral.png")))));
            build.setExpanded(true);
        }

        @Override
        public void onComponentLTSGeralRemove(ProjectDS project, StandardModeling buildDS, Component component) {
            TreeItem<WrapperDS> raiz = findItem(mProjectDSView.getRoot(), project);
            TreeItem<WrapperDS> build = findItem(raiz, buildDS);
            TreeItem<WrapperDS> compGeralLTS = findItem(build, component);
            build.getChildren().remove(compGeralLTS);
        }

    };
    
    private  final StandardModeling.Listener mComponentBuildListener = new StandardModeling.Listener() {

        @Override
        public void onChange(StandardModeling buildDS) {
            for(TreeItem<WrapperDS> p : mProjectDSView.getRoot().getChildren()){
                for(TreeItem<WrapperDS> itm : p.getChildren()){
                    if(itm.getValue().getObject() == buildDS){
                        WrapperDS w = itm.getValue();
                        itm.setValue(null);
                        itm.setValue(w);
                        return;
                    }
                }
            }
        }
        @Override
        public void onBlockCreate(StandardModeling buildDS, Hmsc bbds) {}
        @Override
        public void onBlockRemove(StandardModeling buildDS, Hmsc bbds) {}
        @Override
        public void onTransitionCreate(StandardModeling buildDS, TransitionMSC t) {}
        @Override
        public void onTransitionRemove(StandardModeling buildDS, TransitionMSC t) {}
        @Override
        public void onBlockCreateBMSC(StandardModeling sm, Hmsc hmsc, ComponentDS bmsc) {
            ProjectDS p = getSelectedProjectDS();
            p.addComponent_bMSC(bmsc);
        }
        @Override
        public void onComponentLTSCreate(Component c) {
            ProjectDS p = getSelectedProjectDS();
            p.addComponentFragmentLTS(c);
        }
        @Override
        public void onComponentLTSGeneralCreate(Component c) {
            ProjectDS p = getSelectedProjectDS();
            p.addComponentGeneralLTS(c);
        }
    };

    private final ComponentDS.Listener mComponentBMSC = new ComponentDS.Listener() {

        @Override
        public void onChange(ComponentDS cds) {
            for(TreeItem<WrapperDS> p : mProjectDSView.getRoot().getChildren()){
                for(TreeItem<WrapperDS> itm : p.getChildren()){
                    if(itm.getValue().getObject() == cds){
                        WrapperDS w = itm.getValue();
                        itm.setValue(null);
                        itm.setValue(w);
                        return;
                    }
                }
            }
        }

        @Override
        public void onBlockDSCreated(ComponentDS componentDS, BlockDS ds) {}
        @Override
        public void onBlockDSRemoved(ComponentDS componentDS, BlockDS ds) {}
        @Override
        public void onTransitionCreate(ComponentDS buildDS, TransitionMSC t) {}
        @Override
        public void onTransitionRemove(ComponentDS buildDS, TransitionMSC t) {}

    };
    
    private final Component.Listener mComponentLTS = new Component.Listener() {

        @Override
        public void onChange(Component component) {
            for(TreeItem<WrapperDS> p : mProjectDSView.getRoot().getChildren()){
                for(TreeItem<WrapperDS> itm : p.getChildren()){
                    if(itm.getValue().getObject() == component){
                        WrapperDS w = itm.getValue();
                        itm.setValue(null);
                        itm.setValue(w);
                        return;
                    }
                }
            }
        }

        @Override
        public void onStateCreated(Component component, State state) {}
        @Override
        public void onStateRemoved(Component component, State state) {}
        @Override
        public void onTransitionCreated(Component component, Transition state) {}
        @Override
        public void onTransitionRemoved(Component component, Transition state) {}

    };

    private int id_projeto = 1;

    DesingWindowImplManegerBlockDs dwimbd = new DesingWindowImplManegerBlockDs();
    StandardModelingWindowManager smwm = new StandardModelingWindowManager();
    LtsWindowManager lwm = new LtsWindowManager();
    // public ProjectExplorer mProjectExplorer;


    @Override
    public void onStart(ExtensionManager extensionManager) throws Exception{
        mUserInterface = extensionManager.get(UserInterface.class);
        extension = extensionManager;
        AnchorPane.setTopAnchor(mProjectDSView, 0D);
        AnchorPane.setRightAnchor(mProjectDSView, 0D);
        AnchorPane.setBottomAnchor(mProjectDSView, 0D);
        AnchorPane.setLeftAnchor(mProjectDSView, 0D);
        mUserInterface.getLeftPanel().newTab("UML Projects", mProjectDSView,1, false);
    }
    ExtensionManager extension;
    public ProjectExplorerPluginDS(){
        mListeners = new ArrayList<>();
        mMnuWorkspace = new ContextMenu();
        mMnuProjectDS = new ContextMenu();
        mMnuStandardModeling = new ContextMenu();
        mMnuComponentBMSC = new ContextMenu();
        mMnuComponentLTS = new ContextMenu();
        
        mExtMnuWorkspace = new ExtensibleFXContextMenu(mMnuWorkspace);
        mExtMnuProjectDS = new ExtensibleFXContextMenu(mMnuProjectDS);
        mExtMnuStandardModeling = new ExtensibleFXContextMenu(mMnuStandardModeling);
        mExtMnuBMSC = new ExtensibleFXContextMenu(mMnuComponentBMSC);
        mExtMnuComponentLTS = new ExtensibleFXContextMenu(mMnuComponentLTS);



        mProjectDSView = new TreeView<>();
        mProjectDSView.setRoot(new TreeItem<>());
        mProjectDSView.setShowRoot(false);
        mProjectDSView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        mProjectDSView.setOnMouseClicked((MouseEvent e) -> {
            if (MouseButton.SECONDARY.equals(e.getButton())) {
                if(getSelectedComponentBuildDS() != null){
                    mMnuStandardModeling.show(mProjectDSView, e.getSceneX(),e.getSceneY());
                    mMnuComponentBMSC.hide();
                    mMnuComponentLTS.hide();
                    mMnuProjectDS.hide();
                    mMnuWorkspace.hide();
                }
                else if(getSelectedBMSC() != null){
                    mMnuStandardModeling.hide();
                    mMnuComponentBMSC.show(mProjectDSView, e.getSceneX(),e.getSceneY());
                    mMnuComponentLTS.hide();
                    mMnuProjectDS.hide();
                    mMnuWorkspace.hide();
                }
                else if(getSelectedComponentLTS() != null){
                    mMnuStandardModeling.hide();
                    mMnuComponentBMSC.hide();
                    mMnuComponentLTS.show(mProjectDSView, e.getSceneX(),e.getSceneY());
                    mMnuProjectDS.hide();
                    mMnuWorkspace.hide();
                }
                else if(getSelectedProjectDS() != null){
                    mMnuStandardModeling.hide();
                    mMnuComponentBMSC.hide();
                    mMnuComponentLTS.hide();
                    mMnuProjectDS.show(mProjectDSView, e.getSceneX(),e.getSceneY());
                    mMnuWorkspace.hide();
                }else{
                    mMnuStandardModeling.hide();
                    mMnuComponentBMSC.hide();
                    mMnuComponentLTS.hide();
                    mMnuProjectDS.hide();
                    mMnuWorkspace.show(mProjectDSView, e.getSceneX(),e.getSceneY());
                }
            }else if(e.getClickCount() == 2){
                StandardModeling cbds = getSelectedComponentBuildDS();
                ComponentDS cds = getSelectedBMSC();
                Component c = getSelectedComponentLTS();
                if(cbds != null){
                    try {
                        //StandardModelingWindowManager smwm = new StandardModelingWindowManager();
                        smwm.onStart(extension);
                        smwm.show(cbds);                        
                        mExtMnuStandardModeling.triggerDefaultAction();
                    } catch (Exception ex) {
                        Logger.getLogger(ProjectExplorerPluginDS.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }else if(cds != null){
                    try {
                        //DesingWindowImplManegerBlockDs dwimbd = new DesingWindowImplManegerBlockDs();
                        dwimbd.onStart(extension);
                        dwimbd.show(cds);
                        mExtMnuBMSC.triggerDefaultAction();
                    } catch (Exception ex) {
                        Logger.getLogger(ProjectExplorerPluginDS.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }else if(c != null){
                    try {
                        //LtsWindowManager lwm = new LtsWindowManager();
                        lwm.onStart(extension);
                        lwm.show(c);
                        mExtMnuComponentLTS.triggerDefaultAction();
                    } catch (Exception ex) {
                        Logger.getLogger(ProjectExplorerPluginDS.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }else{
                mMnuStandardModeling.hide();
                mMnuComponentBMSC.hide();
                mMnuComponentLTS.hide();
                mMnuProjectDS.hide();
                mMnuWorkspace.hide();
            }
        });
    }
    
    @Override
    public ExtensibleMenu getMenu() {
        return mExtMnuWorkspace;
    }

    @Override
    public ExtensibleMenu getProjectMSCMenu() {
        return mExtMnuProjectDS;
    }

    @Override
    public ExtensibleMenu getStandarModelingMenu() {
        return mExtMnuStandardModeling;
    }

    @Override
    public ExtensibleMenu getComponentBMSCMenu() {
        return mExtMnuBMSC;
    }

    @Override
    public ExtensibleMenu getComponentLTSMenu() {
        return mExtMnuComponentLTS;
    }

    @Override
    public void open(ProjectDS p) {
        if(p == null){
            throw new IllegalArgumentException("project can not be null!");
        }
        TreeItem<WrapperDS> project = findItem(mProjectDSView.getRoot(), p);
        if(project == null){
            //----------------------------------Raiz-------------------------------------------
            p.addListener(mProjectDSListener);
            p.setID(id_projeto);
            project = new TreeItem<>(new WrapperDS(p), new ImageView(
                    new Image(getClass().getResourceAsStream("/imagens/project/ic_project.png"))));
            //---------------------------------StandardModeling-------------------------------------------
            TreeItem<WrapperDS> compBuildDs = null;
            StandardModeling buildDS = p.getStandardModeling();
            if (buildDS != null){
                buildDS.addListener(mComponentBuildListener);
                buildDS.setID((id_projeto*1000) + 100); // Seta o ID na abertura
                compBuildDs = new TreeItem<>(new WrapperDS(buildDS),new ImageView(
                                    new Image(getClass().getResourceAsStream("/imagens/project/ic_standardModeling.png"))));
                project.getChildren().add(compBuildDs);
            }
            //-------------------------------Component Composed-------------------------------------------------------
            Component composed = p.getLTS_Composed();
            if(composed != null){
                composed.addListener(mComponentLTS);
                composed.setID((id_projeto*1000) + 100 + 1); // Seta ID
                composed.setName("LTS_Composed("+p.getName()+")");
                compBuildDs.getChildren().add(new TreeItem<>(new WrapperDS(composed), new ImageView(
                                new Image(getClass().getResourceAsStream("/imagens/project/ic_componet_lts_geral.png")))));
                compBuildDs.setExpanded(true);
            }
            //-------------------------------------BMSC's---------------------------------------------------
            if(p.getComponentsDS() != null){
                TreeItem<WrapperDS> pastaDS = new TreeItem<>(new WrapperDS("bMSCs"), new ImageView(
                                new Image(getClass().getResourceAsStream("/imagens/project/ic_folders.png"))));
                int id_compds = 0;
                for(ComponentDS componentDs : p.getComponentsDS()){
                    id_compds++;
                    componentDs.addListener(mComponentBMSC);
                    componentDs.setID((id_projeto * 1000) +(200) + id_compds); // Seta id
                    TreeItem<WrapperDS> cds = new TreeItem<>(new WrapperDS(componentDs), new ImageView(
                                    new Image(getClass().getResourceAsStream("/imagens/project/ic_component_ds.png"))));
                    pastaDS.getChildren().add(cds);
                }
                project.getChildren().add(pastaDS);
                pastaDS.setExpanded(true);
            }
            //---------------------------------Fragmentos LTS------------------------------------------------
            if(p.getFragments() != null){
                TreeItem<WrapperDS> fragment = new TreeItem<>(new WrapperDS("Fragments LTS"), new ImageView(
                                new Image(getClass().getResourceAsStream("/imagens/project/ic_folders.png"))));
                int id_lts = 0;
                for(Component component : p.getFragments()){
                    id_lts++;
                    component.addListener(mComponentLTS);
                    component.setID((id_projeto * 1000) + 300 + id_lts);
                    TreeItem<WrapperDS> c = new TreeItem<>(new WrapperDS(component), new ImageView(
                                    new Image(getClass().getResourceAsStream("/imagens/project/ic_component_fragment.png"))));
                    fragment.getChildren().add(c);
                }
                project.getChildren().add(fragment);
                fragment.setExpanded(true);
            }
            mProjectDSView.getRoot().getChildren().add(project);
            id_projeto++;
        }
    }

    @Override
    public void close(ProjectDS p) {
        if(p == null){
            throw new IllegalArgumentException("project can not be null!");
        }
        TreeItem<WrapperDS> project = findItem(mProjectDSView.getRoot(), p);

        if(project != null){
            for(TreeItem<WrapperDS> wds : project.getChildren()) {
                if (wds.getValue().getObject() instanceof StandardModeling) {
                    smwm.close((StandardModeling) wds.getValue().getObject());
                    lwm.close((Component)wds.getChildren().get(0).getValue().getObject());
                }else {
                    for(TreeItem<WrapperDS> wds2 : wds.getChildren()) {
                        if(wds2.getValue().getObject() instanceof Component){
                            lwm.close((Component)wds2.getValue().getObject());
                        }else if (wds2.getValue().getObject() instanceof ComponentDS){
                            dwimbd.close((ComponentDS)wds2.getValue().getObject());
                        }
                    }
                }

            }
            if( mProjectDSView.getSelectionModel().getSelectedItem() != null){
                mProjectDSView.getSelectionModel().clearSelection();
            }
            mProjectDSView.getRoot().getChildren().removeAll(project);
        }
    }

    public void rename(ProjectDS p){
        if(p == null){
            Alert alert = new Alert(Alert.AlertType.INFORMATION,"Select a project", ButtonType.OK);
            alert.show();
        }
        TreeItem<WrapperDS> project = findItem(mProjectDSView.getRoot(), p);
        for(TreeItem<WrapperDS> wds : project.getChildren()){
            String [] nome = wds.getValue().toString().split("[(]");
            wds.getValue().set(nome[0]+"("+p.getName()+")");

            if(wds.getValue().getObject() instanceof StandardModeling){
                ((StandardModeling) wds.getValue().getObject()).setName(nome[0]+"("+p.getName()+")");
                for(TreeItem<WrapperDS> wds2 : wds.getChildren()){
                    String[]nome2 = wds2.getValue().toString().split("[(]");
                    wds2.getValue().set(nome2[0]+"("+p.getName()+")");
                }
            }
        }
        // Listener,onChange, do Standard não funcionando. Necessário para renomear a Tab
        ExtensibleTabPane mCenterPanel = ((UserInterface)extension.get(UserInterface.class)).getCenterPanel();
        mCenterPanel.renameTab(p.getStandardModeling().getID(), p.getStandardModeling().getName());
        clear2();
    }

    public void removeBMSC( ComponentDS bMSC){
                TreeItem<WrapperDS> item = mProjectDSView.getSelectionModel().getSelectedItem();
                item.getParent().getChildren().remove(item);
                dwimbd.close(bMSC);
                clearSelecao();
                return;
    }

    @Override
    public void removeFragmetsLTS() {
        TreeItem<WrapperDS> project = findItem(mProjectDSView.getRoot(), getSelectedProjectDS());
        //TreeItem<WrapperDS> PastaFrag = project.getChildren().get(2);
        project.getChildren().remove(2);
    }

    @Override
    public void clearSelecao() {

    }

    public void clear2(){
        mProjectDSView.refresh();
    }


    @Override
    public ProjectDS getSelectedProjectDS() {
        TreeItem<WrapperDS> projeto = mProjectDSView.getSelectionModel().getSelectedItem();
        if(projeto == null){
            return null;
        }
        Object obj = projeto.getValue().getObject();
        if(obj instanceof StandardModeling){
            projeto = projeto.getParent();
        }
        else if(obj instanceof ComponentDS){
            projeto = projeto.getParent().getParent();
        }
        else if(obj instanceof Component){
            projeto = projeto.getParent().getParent();
        }else if( obj instanceof String){
            projeto = projeto.getParent();
        }
        return (ProjectDS) projeto.getValue().getObject();
    }

    @Override
    public StandardModeling getSelectedComponentBuildDS() {
        TreeItem<WrapperDS> buildDs = mProjectDSView.getSelectionModel().getSelectedItem();
        if(buildDs == null){
            return null;
        }
        Object obj = buildDs.getValue().getObject();
        return obj instanceof StandardModeling ? (StandardModeling)obj : null;
    }

    @Override
    public ComponentDS getSelectedBMSC() {
        TreeItem<WrapperDS> ds = mProjectDSView.getSelectionModel().getSelectedItem();
        if(ds == null){
            return null;
        }
        Object obj = ds.getValue().getObject();
        return obj instanceof ComponentDS ? (ComponentDS)obj : null;
    }

    @Override
    public Component getSelectedComponentLTS() {
        TreeItem<WrapperDS> lts = mProjectDSView.getSelectionModel().getSelectedItem();
        if(lts == null){
            return null;
        }
        Object obj = lts.getValue().getObject();
        return obj instanceof Component ? (Component)obj : null;
    }

    @Override
    public List<ProjectDS> getSelectedProjectsDS() {
        List<ProjectDS> aux = new ArrayList<>();
        for(TreeItem<WrapperDS> p : mProjectDSView.getSelectionModel().getSelectedItems()){
            Object obj = p.getValue().getObject();
            if(obj instanceof ProjectDS){
                aux.add((ProjectDS)obj);
            }
        }
        return aux;
    }

    @Override
    public List<ComponentDS> getSelectedBMSCs() {
        List<ComponentDS> aux = new ArrayList<>();
        for(TreeItem<WrapperDS> cds : mProjectDSView.getSelectionModel().getSelectedItems()){
            Object obj = cds.getValue().getObject();
            if(obj instanceof ComponentDS){
                aux.add((ComponentDS)obj);
            }
        }
        return aux;
    }

    @Override
    public List<Component> getSelectedComponentsLTS() {
        List<Component> aux = new ArrayList<>();
        for(TreeItem<WrapperDS> c : mProjectDSView.getSelectionModel().getSelectedItems()){
            Object obj = c.getValue().getObject();
            if(obj instanceof Component){
                aux.add((Component)obj);
            }
        }
        return aux;
    }
    
    @Override
    public List<ProjectDS> getAllProjectsDS() {
        List<ProjectDS> aux = new ArrayList<>();
        for (TreeItem<WrapperDS> itm : mProjectDSView.getRoot().getChildren()) {
            Object obj = itm.getValue().getObject();
            if (obj instanceof ProjectDS) {
                aux.add((ProjectDS) obj);
            }
        }
        return aux;
    }
    
    @Override
    public List<ComponentDS> getAll_BMSC(){ //retorna somente os bMSC do projeto selecionado     
        return (List<ComponentDS>) getSelectedProjectDS().getComponentsDS();
    }

    @Override
    public void addListener(Listener l) {
        mListeners.add(l);
    }

    @Override
    public void removeListener(Listener l) {
        mListeners.remove(l);
    }
    
    private TreeItem<WrapperDS> findItem(TreeItem<WrapperDS> root, Object obj) {
        for (TreeItem<WrapperDS> itm : root.getChildren()) {
            if (itm.getValue().getObject() == obj) {
                return itm;
            }
        }
        return null;
    }

    public boolean open_BMSC(ComponentDS componentDS){
        if(!getAll_BMSC().contains(componentDS)){
            return false;
        }
        try {
            dwimbd.show(componentDS);
        }catch (IllegalStateException e){
            try {
                dwimbd.onStart(extension);
            }catch (Exception e1){
                System.out.println("ERROR ao Startar");
            }
            dwimbd.show(componentDS);
        }catch(NullPointerException e){
            try{
                smwm.onStart(extension);
            }catch (Exception e1){
                System.out.println("ERROR AO START 2");
            }
            return false;
        }
        return true;
    }
    
}

class WrapperDS {
    
    private final Object mObj;
    
    public WrapperDS(Object obj) {
        this.mObj = obj;
    }
    
    Object getObject() {
        return mObj;
    }
    
    public void set(String s) {
        if (mObj instanceof ProjectDS) {
            ((ProjectDS) mObj).setName(s);
        } 
        else if(mObj instanceof StandardModeling){
            ((StandardModeling) mObj).setName(s);
        }
        else if(mObj instanceof ComponentDS){
            ((ComponentDS) mObj).setName(s);
        }
        else if(mObj instanceof Component){
            ((Component) mObj).setName(s);
        }
    }
    
    @Override
    public String toString(){
        String s = "";
        if(mObj instanceof ProjectDS){
            s = ((ProjectDS) mObj).getName();
        }
        else if(mObj instanceof StandardModeling){
            s = ((StandardModeling) mObj).getName();
        }
        else if(mObj instanceof ComponentDS){
            s = ((ComponentDS) mObj).getName();
        }
        else if(mObj instanceof Component){
            s = ((Component) mObj).getName();
        }
        else{
            s = ((String) mObj);
        }
        
        return s;
    }
}
