/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uece.lotus.uml.api.viewer.transition;


import br.uece.lotus.uml.api.ds.BlockDS;
import br.uece.lotus.uml.api.viewer.bMSC.BlockDSView;
import br.uece.lotus.uml.api.viewer.hMSC.HmscView;
import br.uece.lotus.viewer.Geom;
import br.uece.lotus.viewer.Seta;
import br.uece.lotus.viewer.StyleBuilder;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.NumberBinding;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;


/**
 *
 * @author Bruno Barbosa
 */
public class LineTransitionMSCViewImpl extends TransitionMSCViewImpl{

    private final Label mRotulo;
    private final Seta mSeta;
    private final Line mLine;

    public LineTransitionMSCViewImpl() {
        mLine = new Line();
        getChildren().add(mLine);
        mSeta = new Seta();
        getChildren().add(mSeta);
        mRotulo = new Label();
        getChildren().add(mRotulo);
    }
    
    @Override
    protected void prepareView() {
        Region origem = null;
        Region destino = null;
        
        NumberBinding meioCaminhoX = mLine.endXProperty().subtract(mLine.startXProperty()).divide(2);
        NumberBinding meioCaminhoY = mLine.endYProperty().subtract(mLine.startYProperty()).divide(2);

        switch (mValueType) {
            case "hMSC":{
                try {
                    origem = (Region) hMscSource.getNode();
                    destino = (Region) hMscDestiny.getNode();
                } catch (NullPointerException e) {
                    HmscView src = (HmscView) srcHMSC.getValue("view");
                    HmscView dst = (HmscView) dstHMSC.getValue("view");
                    origem = (Region) src.getNode();
                    destino = (Region) dst.getNode();
                }
                if(origem != null && destino != null){
                    DoubleBinding origemX = origem.layoutXProperty().add(origem.widthProperty().divide(2));
                    DoubleBinding origemY = origem.layoutYProperty().add(origem.heightProperty().divide(2));
                    DoubleBinding destinoX = destino.layoutXProperty().add(destino.widthProperty().divide(2));
                    DoubleBinding destinoY = destino.layoutYProperty().add(destino.heightProperty().divide(2));

                    mSeta.rotateProperty().bind(Geom.angle(origem, destino));
                    mSeta.rotateProperty().bind(new Geom.CartesianCase(origem, destino)
                                    .first(Geom.angle(origem, destino))
                                    .second(Geom.angle(origem, destino).add(180))
                                    .thirth(Geom.angle(origem, destino).add(180))
                                    .fourth(Geom.angle(origem, destino))
                                    .firstAndSecond(Geom.angle(origem, destino).add(180))
                                    .secondAndThirth(Geom.angle(origem, destino).add(180))
                                    .thirthAndFourth(Geom.angle(origem, destino))
                    );

                    mLine.startXProperty().bind(origemX);
                    mLine.startYProperty().bind(origemY);
                    mLine.endXProperty().bind(destinoX);
                    mLine.endYProperty().bind(destinoY);

                    mRotulo.layoutXProperty().bind(origemX.add(meioCaminhoX).subtract(mRotulo.widthProperty().divide(2)));
                    mRotulo.layoutYProperty().bind(origemY.add(meioCaminhoY).subtract(25));

                    mSeta.layoutXProperty().bind(origemX.add(meioCaminhoX));
                    mSeta.layoutYProperty().bind(origemY.add(meioCaminhoY));
                }
                break;  
            }
            case "bMSC":{
                try{
                    origem = (Region) bMscSource.getNode();
                    destino = (Region) bMscDestiny.getNode();
                }catch(NullPointerException e){
                    BlockDSView src = (BlockDSView) srcBMSC.getValue("view");
                    BlockDSView dst = (BlockDSView) dstBMSC.getValue("view");
                    origem = (Region) src.getNode();
                    destino = (Region) dst.getNode();
                }
                if(origem != null && destino != null){
                    DoubleBinding origemX = origem.layoutXProperty().add(origem.widthProperty().divide(2));
                    DoubleBinding origemY = origem.layoutYProperty().add(origem.heightProperty().divide(2));
                    DoubleBinding destinoX = destino.layoutXProperty().add(destino.widthProperty().divide(2));
                    DoubleBinding destinoY = destino.layoutYProperty().add(destino.heightProperty().divide(2));

                    mSeta.rotateProperty().bind(Geom.angle(origem, destino));
                    mSeta.rotateProperty().bind(new Geom.CartesianCase(origem, destino)
                                    .first(Geom.angle(origem, destino))
                                    .second(Geom.angle(origem, destino).add(180))
                                    .thirth(Geom.angle(origem, destino).add(180))
                                    .fourth(Geom.angle(origem, destino))
                                    .firstAndSecond(Geom.angle(origem, destino).add(180))
                                    .secondAndThirth(Geom.angle(origem, destino).add(180))
                                    .thirthAndFourth(Geom.angle(origem, destino))
                    );

                    mLine.startXProperty().bind(origemX);
                    mLine.endXProperty().bind(destinoX);

                    mRotulo.layoutXProperty().bind(origemX.add(meioCaminhoX).subtract(mRotulo.widthProperty().divide(2)));
                    mRotulo.layoutYProperty().bind(mLine.endYProperty().subtract(mRotulo.heightProperty()));

                    mSeta.layoutXProperty().bind(mLine.endXProperty().subtract(4));
                    mSeta.layoutYProperty().bind(mLine.endYProperty());

                }
                break;
            } 
        }
        
        
    }

    @Override
    protected void updateView() {
    /*    if (mValueType.equals("bMSC")){  Caso as cores sejam diferente
            mLine.setStyle(StyleBuilder.stroke("red", mTransition.getWidth()));
            mRotulo.setStyle(StyleBuilder.font(mTransition.getTextColor(), mTransition.getTextStyle(), mTransition.getTextSize()));
            mRotulo.setText(getComputedLabel());
        }else {
    */      mLine.setStyle(StyleBuilder.stroke(mTransition.getColor(), mTransition.getWidth()));
            mRotulo.setStyle(StyleBuilder.font(mTransition.getTextColor(), mTransition.getTextStyle(), mTransition.getTextSize()));
            mRotulo.setText(getComputedLabel());
    //    }
    }

    @Override
    public boolean isInsideBounds_hMSC(Point2D point) {
        return (mSeta.localToScene(Point2D.ZERO).distance(point) < 8);
    }

    @Override
    public boolean isInsideBounds_bMSC(Circle circle){
        if(circle.getBoundsInParent().intersects(mRotulo.getBoundsInParent())){
            return true;
        }
        else if(circle.getBoundsInParent().intersects(mSeta.getBoundsInParent())){
            return true;
        }
        else if(circle.getBoundsInParent().intersects(mLine.getBoundsInParent())){
            return true;
        }
        return false;
    }

    @Override
    public Line getLineTransition(){
        return mLine;
    }
    
}
