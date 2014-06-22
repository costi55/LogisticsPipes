package logisticspipes.modules;

import java.util.List;

import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.IPipeServiceProvider;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.basic.CoreRoutedPipe.ItemSendMode;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.tuples.Triplet;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ModuleElectricBuffer extends LogisticsModule {
	private IInventoryProvider _invProvider;
	private IPipeServiceProvider _service;



	private IWorldProvider _world;

	private int currentTickCount = 0;
	private int ticksToAction = 80;

	public ModuleElectricBuffer() {}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {}

	@Override
	public void registerHandler(IInventoryProvider invProvider, IWorldProvider world, IPipeServiceProvider service) {
		_invProvider = invProvider;
		_service = service;
		_world = world;

	}

	
	@Override 
	public final int getX() {
		return this._service.getX();
	}
	@Override 
	public final int getY() {
		return this._service.getY();
	}
	
	@Override 
	public final int getZ() {
		return this._service.getZ();
	}

	private final SinkReply _sinkReply = new SinkReply(FixedPriority.ElectricBuffer, 0, true, false, 1, 0);
	@Override
	public SinkReply sinksItem(ItemIdentifier stack, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit) {
		if (bestPriority >= FixedPriority.ElectricBuffer.ordinal()) return null;
		if (SimpleServiceLocator.IC2Proxy.isElectricItem(stack.makeNormalStack(1))) {
			if (_service.canUseEnergy(1)) {
				return _sinkReply;
			}
		}
		return null;
	}

	@Override
	public LogisticsModule getSubModule(int slot) {
		return null;
	}

	@Override
	public void tick() {
		if (++currentTickCount < ticksToAction) return;
		currentTickCount = 0;

		IInventoryUtil inv = _invProvider.getPointedInventory(true);
		if (inv == null) return;
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if (stack == null) continue;
			if (SimpleServiceLocator.IC2Proxy.isElectricItem(stack)) {
				Triplet<Integer, SinkReply, List<IFilter>> reply = SimpleServiceLocator.logisticsManager.hasDestinationWithMinPriority(ItemIdentifier.get(stack), _invProvider.getSourceID(), true, FixedPriority.ElectricManager);
				if(reply == null) continue;
				_service.spawnParticle(Particles.OrangeParticle, 2);
				_invProvider.sendStack(inv.decrStackSize(i, 1), reply, ItemSendMode.Normal);
				return;
			}
			continue;
		}
	}
	@Override
	public boolean hasGenericInterests() {
		return true;
	}

	@Override
	public List<ItemIdentifier> getSpecificInterests() {
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
	@SideOnly(Side.CLIENT)
	public Icon getIconTexture(IconRegister register) {
		return register.registerIcon("logisticspipes:itemModule/ModuleElectricBuffer");
	}
}
