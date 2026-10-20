package net.kafgchan.kitsunebi.forge.compat;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;
/** Match vanilla head-item transforms so the existing mask model follows the head. */
public final class CuriosMaskRenderer implements ICurioRenderer {
 public <T extends LivingEntity,M extends EntityModel<T>> void render(ItemStack stack,SlotContext context,PoseStack pose,RenderLayerParent<T,M> parent,MultiBufferSource buffers,int light,float limbSwing,float limbAmount,float partial,float age,float yaw,float pitch){
  if(!(parent.getModel() instanceof HumanoidModel<?> humanoid))return;
  pose.pushPose();humanoid.head.translateAndRotate(pose);
  pose.translate(0,-.25,0);pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180));pose.scale(.625F,-.625F,-.625F);
  Minecraft.getInstance().getItemRenderer().renderStatic(stack,ItemDisplayContext.HEAD,light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,pose,buffers,context.entity().level(),context.entity().getId());
  pose.popPose();
 }
}
