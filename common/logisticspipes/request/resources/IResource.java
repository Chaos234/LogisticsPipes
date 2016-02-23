package logisticspipes.request.resources;

import logisticspipes.network.LPDataOutputStream;
import logisticspipes.proxy.computers.interfaces.ILPCCTypeHolder;
import logisticspipes.routing.IRouter;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;

/**
 * With Destination and amount
 */
public interface IResource extends ILPCCTypeHolder {

	int getRequestedAmount();

	IRouter getRouter();

	boolean matches(ItemIdentifier itemType);

	IResource clone(int multiplier);

	void writeData(LPDataOutputStream data) throws IOException;

	boolean mergeForDisplay(IResource resource, int withAmount); //Amount overrides existing amount inside the resource

	IResource copyForDisplayWith(int amount);

	@SideOnly(Side.CLIENT)
	String getDisplayText(ColorCode missing);

	ItemIdentifierStack getDisplayItem();

	enum ColorCode {
		NONE,
		MISSING,
		SUCCESS;
	}
}
