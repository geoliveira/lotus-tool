/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uece.lotus.uml.api.viewer.builder;

/**
 *
 * @author Bruno Barbosa
 */
public class BlockBuildDSViewFactory implements BlockBuildDSView.Factory{

    @Override
    public BlockBuildDSView create() {
        return new BlockBuildDSViewImpl();
    }
    
}