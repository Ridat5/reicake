package com.reiasu.reiparticlesapi.exceptions;

/**
 * Thrown when a render pipeline FBO color channel already has an input bound.
 */
public final class RenderPipeInputException extends Exception {

    public RenderPipeInputException(int inputFBO, int inputChannel) {
        super("Render pipeline fbo: " + inputFBO + " color channel " + inputChannel + " can only have one input");
    }
}
