package net.bauxite_ltk.immersive_metallurgy.block.transporter.api.resourceHandler;

public interface ICompactUniHandler<R, Handler>  extends IUniHandler<R> {
    Handler getCompactParent();
}
