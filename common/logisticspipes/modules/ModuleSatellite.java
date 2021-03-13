package logisticspipes.modules;

import java.util.Objects;
import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import logisticspipes.utils.item.ItemIdentifier;
import network.rs485.logisticspipes.connection.ConnectionType;
import network.rs485.logisticspipes.connection.NeighborTileEntity;
import network.rs485.logisticspipes.world.WorldCoordinatesWrapper;

//IHUDModuleHandler,
public class ModuleSatellite extends LogisticsModule {

	private final SinkReply _sinkReply = new SinkReply(FixedPriority.ItemSink, 0, true, false, 1, 0, null);

	@Override
	public SinkReply sinksItem(@Nonnull ItemStack stack, ItemIdentifier item, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit, boolean forcePassive) {
		if (bestPriority > _sinkReply.fixedPriority.ordinal() || (bestPriority == _sinkReply.fixedPriority.ordinal() && bestCustomPriority >= _sinkReply.customPriority)) {
			return null;
		}
		final int itemCount = spaceFor(stack, item, includeInTransit);
		if (itemCount > 0) {
			return new SinkReply(_sinkReply, itemCount);
		} else {
			return null;
		}
	}

	private int spaceFor(@Nonnull ItemStack stack, ItemIdentifier item, boolean includeInTransit) {
		WorldCoordinatesWrapper worldCoordinates = new WorldCoordinatesWrapper(_world.getWorld(), _service.getPos());

		int count = worldCoordinates.connectedTileEntities(ConnectionType.ITEM)
				.map(adjacent -> adjacent.sneakyInsertion().from(getUpgradeManager()))
				.map(NeighborTileEntity::getInventoryUtil)
				.filter(Objects::nonNull)
				.map(util -> util.roomForItem(stack))
				.reduce(Integer::sum).orElse(0);

		if (includeInTransit) {
			count -= _service.countOnRoute(item);
		}
		return count;
	}

	@Override
	public void readFromNBT(@Nonnull NBTTagCompound nbttagcompound) {}

	@Override
	public void writeToNBT(@Nonnull NBTTagCompound nbttagcompound) {}

	@Override
	public void tick() {}

	@Override
	public boolean hasGenericInterests() {
		return false;
	}

	@Override
	public boolean interestedInAttachedInventory() {
		return false;
		// when we are default we are interested in everything anyway, otherwise we're only interested in our filter.
	}

	@Override
	public boolean interestedInUndamagedID() {
		return false;
	}

	@Override
	public boolean recievePassive() {
		return false;
	}

}
