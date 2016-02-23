package logisticspipes.proxy.specialconnection;

import logisticspipes.interfaces.routing.ISpecialTileConnection;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.UtilEnumFacing;
import logisticspipes.utils.tuples.LPPosition;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import java.util.ArrayList;
import java.util.List;

public class EnderIOHyperCubeConnection implements ISpecialTileConnection {

	@Override
	public boolean init() {
		return SimpleServiceLocator.enderIOProxy.isEnderIO();
	}

	@Override
	public boolean isType(TileEntity tile) {
		return SimpleServiceLocator.enderIOProxy.isHyperCube(tile);
	}

	@Override
	public List<TileEntity> getConnections(TileEntity tile) {
		boolean onlyOnePipe = false;
		for (EnumFacing direction : UtilEnumFacing.VALID_DIRECTIONS) {
			LPPosition p = new LPPosition(tile);
			p.moveForward(direction);
			TileEntity canidate = p.getTileEntity(tile.getWorld());
			if (canidate instanceof LogisticsTileGenericPipe && MainProxy.checkPipesConnections(tile, canidate, direction)) {
				if (onlyOnePipe) {
					onlyOnePipe = false;
					break;
				} else {
					onlyOnePipe = true;
				}
			}
		}
		if (!onlyOnePipe || !SimpleServiceLocator.enderIOProxy.isSendAndReceive(tile)) {
			return new ArrayList<TileEntity>(0);
		}
		List<? extends TileEntity> connections = SimpleServiceLocator.enderIOProxy.getConnectedHyperCubes(tile);
		List<TileEntity> list = new ArrayList<TileEntity>();
		for (TileEntity connected : connections) {
			if (!SimpleServiceLocator.enderIOProxy.isSendAndReceive(connected)) {
				continue;
			}
			LogisticsTileGenericPipe pipe = null;
			for (EnumFacing direction : UtilEnumFacing.VALID_DIRECTIONS) {
				LPPosition p = new LPPosition(connected);
				p.moveForward(direction);
				TileEntity canidate = p.getTileEntity(tile.getWorld());
				if (canidate instanceof LogisticsTileGenericPipe && MainProxy.checkPipesConnections(connected, canidate, direction)) {
					if (pipe != null) {
						pipe = null;
						break;
					} else {
						pipe = (LogisticsTileGenericPipe) canidate;
					}
				}
			}
			if (pipe != null && pipe.pipe instanceof CoreRoutedPipe) {
				list.add(pipe);
			}
		}
		if (list.size() == 1) {
			return list;
		} else {
			return new ArrayList<TileEntity>(0);
		}
	}

	@Override
	public boolean needsInformationTransition() {
		return true;
	}

	@Override
	public void transmit(TileEntity tile, IRoutedItem data) {
		List<TileEntity> list = getConnections(tile);
		if (list.size() < 1) {
			return;
		}
		TileEntity pipe = list.get(0);
		if (pipe instanceof LogisticsTileGenericPipe) {
			((CoreRoutedPipe) ((LogisticsTileGenericPipe) pipe).pipe).queueUnroutedItemInformation(data.getItemIdentifierStack().clone(), data.getInfo());
		} else {
			new RuntimeException("Only LP pipes can be next to Tesseracts to queue item information").printStackTrace();
		}
	}
}
