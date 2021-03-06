package br.uece.lotus.uml.app.runtime.utils;



import br.uece.lotus.uml.api.ds.*;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ProjectXMLProjectMSCMSCConverterMSC implements ProjectMSCConverter<Path> {

    private ProjectDS projectDS;
    private  DocumentBuilderFactory factory;
    private  DocumentBuilder documentBuilder;
    private StandardModeling stantardModeling;

    public ProjectXMLProjectMSCMSCConverterMSC() {
         factory = DocumentBuilderFactory.newInstance();
        try {
                documentBuilder = factory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
        }

    }




    @Override
    public ProjectDS toConverter(Path path) throws Exception {
        // fazer o tratamento para quando for importanto um arquivo xml vazio ou algo parecido
        InputStream inputStream = Files.newInputStream(path);

        Document document = documentBuilder.parse(inputStream);
        document.getDocumentElement().normalize();
        parseAboutProjectMSCCustom(document);
        return projectDS;
    }


    private void parseAboutProjectMSCCustom(Document document) {

        projectDS = new ProjectDS();

        NodeList projectMscNodeList = document.getElementsByTagName("project-msc");
        Node projectMscNode =  projectMscNodeList.item(0);
        Element projectMscElement = (Element) projectMscNode;
        String nameProjectMsc = projectMscElement.getAttribute("name");
        projectDS.setName(nameProjectMsc);

        parseAboutStandardModelingCustom(document);



    }

    private void parseAboutStandardModelingCustom(Document document) {
        stantardModeling = new StandardModeling();
        projectDS.setComponentBuildDS(stantardModeling);

        parseAboutHMSC(document);

        parseAboutTransitionHMSC(document);
    }



    private void parseAboutTransitionHMSC(Document document) {
        NodeList transitionHMSCNodeList = document.getElementsByTagName("TransitionHMSC");
        Node transitionHMSCNode;
        Element transitionHMSCElement;
        TransitionMSC transitionMSC;
        Integer srcHMSCInInt,dstHMSCInInt;
        String srcHMSCInString,dstHMSCInString, label;
        Double probability;

        Hmsc srcHMSCCustom, dstHMSCCustom;

        for(int i = 0; i < transitionHMSCNodeList.getLength(); i++ ){
           transitionHMSCNode = transitionHMSCNodeList.item(i);
           transitionHMSCElement = (Element) transitionHMSCNode;
           srcHMSCInString = transitionHMSCElement.getAttribute("from");
           srcHMSCInInt = Integer.valueOf(srcHMSCInString);

           dstHMSCInString = transitionHMSCElement.getAttribute("to");
           dstHMSCInInt = Integer.valueOf(dstHMSCInString);

           label = transitionHMSCElement.getAttribute("label");

//           if(label == null){
//               label = srcHMSCInString.concat("&").concat(dstHMSCInString);
//           }

           probability = Double.valueOf(transitionHMSCElement.getAttribute("prob"));

           srcHMSCCustom = stantardModeling.getBlocoByID(srcHMSCInInt);
           dstHMSCCustom = stantardModeling.getBlocoByID(dstHMSCInInt);


           new TransitionMSC.Builder(stantardModeling, new TransitionMSC(srcHMSCCustom,dstHMSCCustom))
                   .setLabel(label)
                   .setProbability(probability)
                   .create();
        }

    }

    private void parseAboutHMSC(Document document) {

        Node HMSCNode;
        Element HMSCElement;
        String HMSCLabel, HMSCIdInString;
        Integer HMSCIdInInt;
        Hmsc hmsc;

        NodeList HMSCNodeList = document.getElementsByTagName("hMSC");
        for(int i = 0; i<HMSCNodeList.getLength(); i++){
            HMSCNode = HMSCNodeList.item(i);
            HMSCElement = ((Element) HMSCNode);
            HMSCIdInString = HMSCElement.getAttribute("id");
            HMSCIdInInt = Integer.valueOf(HMSCIdInString);
            HMSCLabel = HMSCElement.getAttribute("label");

            hmsc = new Hmsc(stantardModeling);
            hmsc.setID(HMSCIdInInt);
            hmsc.setLabel(HMSCLabel);


        }

    }


    @Override
    public void toUpdate(ProjectDS projectDSIn, Path pathSrc) throws Exception {
        InputStream inputStream = Files.newInputStream(pathSrc);

        Document document = documentBuilder.parse(inputStream);
        document.getDocumentElement().normalize();


        NodeList transitionHMSCNodeList = document.getElementsByTagName("TransitionHMSC");
        Node transitionMSCNode;
        Element transitionHMSCElement;
        Integer srcHMSCInInt,dstHMSCInInt;
        String srcHMSCInString,dstHMSCInString;

        for(int i = 0; i < transitionHMSCNodeList.getLength(); i++ ){
            transitionMSCNode = transitionHMSCNodeList.item(i);
            transitionHMSCElement = (Element) transitionMSCNode;
            srcHMSCInString = transitionHMSCElement.getAttribute("from");
            srcHMSCInInt = Integer.valueOf(srcHMSCInString);

            dstHMSCInString = transitionHMSCElement.getAttribute("to");
            dstHMSCInInt = Integer.valueOf(dstHMSCInString);

            TransitionMSC currentTransitionMSC = stantardModeling.getTransitionMSC(srcHMSCInInt, dstHMSCInInt);

            if(currentTransitionMSC == null){
                throw new Exception("TransitionHMSCCustom from xml not found!");
            }else {

                updateProbabilityInTranstionMSCNode(transitionMSCNode, currentTransitionMSC);
            }

        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(new File(pathSrc.toUri()));
        transformer.transform(source, result);
    }



    private void updateProbabilityInTranstionMSCNode(Node transitionMSCNode, TransitionMSC transitionMSC) {
        NamedNodeMap attr = transitionMSCNode.getAttributes();
        Node probAttr = attr.getNamedItem("prob");
        Double updateProbabilityDouble = transitionMSC.getProbability();
        String updateProbabilityString = String.valueOf(updateProbabilityDouble);
        probAttr.setTextContent(updateProbabilityString);
    }

    @Override
    public Path toUndo(ProjectDS projectDSIn) throws Exception {
        return null;
    }


}
