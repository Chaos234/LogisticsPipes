package logisticspipes.modules;

import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.pipes.PipeLogisticsChassi.ChassiTargetInformation;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import logisticspipes.utils.item.ItemIdentifier;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;

import java.util.Collection;

public class ModuleEnchantmentSink extends LogisticsModule {

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {}



	private SinkReply _sinkReply;

	@Override
	public void registerPosition(ModulePositionType slot, int positionInt) {
		super.registerPosition(slot, positionInt);
		_sinkReply = new SinkReply(FixedPriority.EnchantmentItemSink, 0, true, false, 1, 0, new ChassiTargetInformation(getPositionInt()));
	}

	@Override
	public BlockPos getblockpos() {
		if (slot.isInWorld()){
			return  _service.getblockpos();
		}else {
			return  null;
		}

	}

	@Override
	public SinkReply sinksItem(ItemIdentifier item, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit) {
		// check to see if a better route is already found
		// Note: Higher MKs are higher priority
		if (bestPriority > _sinkReply.fixedPriority.ordinal() || (bestPriority == _sinkReply.fixedPriority.ordinal() && bestCustomPriority >= _sinkReply.customPriority)) {
			return null;
		}

		//check to see if item is enchanted
		if (item.makeNormalStack(1).isItemEnchanted()) {
			return _sinkReply;
		}
		return null;
	}

	@Override
	public LogisticsModule getSubModule(int slot) {
		return null;
	}

	@Override
	public void tick() {}

	@Override
	/*
	 * We will check every item return true
	 * @see logisticspipes.modules.LogisticsModule#hasGenericInterests()
	 */
	public boolean hasGenericInterests() {
		return true;
	}

	@Override
	/*
	 * Null return as checking all items
	 * @see logisticspipes.modules.LogisticsModule#getSpecificInterests()
	 */
	public Collection<ItemIdentifier> getSpecificInterests() {
		return null;
	}

	@Override
	public boolean interestedInAttachedInventory() {
		return false;
	}

	@Override
	public boolean interestedInUndamagedID() {
		return true;
	}

	@Override
	public boolean recievePassive() {
		return true;
	}

	@Override
	public boolean hasEffect() {
		return true;
	}
}
