/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.hose;

import java.util.concurrent.RecursiveAction;
import net.minecraft.server.Entity;
import net.minecraft.server.World;


/**
 *
 * @author softpak
 */
public class Tick_Pool extends RecursiveAction {
    Entity entity;
    World world;
    
    public Tick_Pool(Entity entity){
        this.entity = entity;
        //this.world = world;
        
    }
    
    @Override
    protected void compute() {
        entity.U();
    }
    
}
