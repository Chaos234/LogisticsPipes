package logisticspipes.utils;

import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.world.ILockableContainer;

/**
 * A large chest helper implementing hashCode and equals
 * 
 * @author artforz
 */
public class InventoryLargeChestLogistics extends InventoryLargeChest {

	private final ILockableContainer _upperChest;
	private final ILockableContainer _lowerChest;

	public InventoryLargeChestLogistics(String par1Str, ILockableContainer par2IInventory, ILockableContainer par3IInventory) {
		super(par1Str, par2IInventory, par3IInventory);
		_upperChest = par2IInventory;
		_lowerChest = par3IInventory;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof InventoryLargeChestLogistics)) {
			return false;
		}
		InventoryLargeChestLogistics b = (InventoryLargeChestLogistics) obj;
		return (_upperChest == b._upperChest && _lowerChest == b._lowerChest);
	}

	@Override
	public int hashCode() {
		return _upperChest.hashCode() ^ _lowerChest.hashCode();
	}
}
