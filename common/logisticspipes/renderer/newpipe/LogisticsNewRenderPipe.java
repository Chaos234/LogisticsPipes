package logisticspipes.renderer.newpipe;

import logisticspipes.LPConstants;
import logisticspipes.LogisticsPipes;
import logisticspipes.config.PlayerConfig;
import logisticspipes.pipes.PipeBlockRequestTable;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.object3d.interfaces.I3DOperation;
import logisticspipes.proxy.object3d.interfaces.IIconTransformation;
import logisticspipes.proxy.object3d.interfaces.IModel3D;
import logisticspipes.proxy.object3d.interfaces.IVec3;
import logisticspipes.proxy.object3d.operation.LPScale;
import logisticspipes.proxy.object3d.operation.LPTranslation;
import logisticspipes.proxy.object3d.operation.LPUVTransformationList;
import logisticspipes.proxy.object3d.operation.LPUVTranslation;
import logisticspipes.renderer.state.PipeRenderState;
import logisticspipes.textures.Textures;
import logisticspipes.utils.UtilEnumFacing;
import logisticspipes.utils.tuples.LPPosition;
import logisticspipes.utils.tuples.Pair;
import logisticspipes.utils.tuples.Quartet;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.Map.Entry;

public class LogisticsNewRenderPipe {

	enum Edge {
		Upper_North(EnumFacing.UP, EnumFacing.NORTH),
		Upper_South(EnumFacing.UP, EnumFacing.SOUTH),
		Upper_East(EnumFacing.UP, EnumFacing.EAST),
		Upper_West(EnumFacing.UP, EnumFacing.WEST),
		Lower_North(EnumFacing.DOWN, EnumFacing.NORTH),
		Lower_South(EnumFacing.DOWN, EnumFacing.SOUTH),
		Lower_East(EnumFacing.DOWN, EnumFacing.EAST),
		Lower_West(EnumFacing.DOWN, EnumFacing.WEST),
		Middle_North_West(EnumFacing.NORTH, EnumFacing.WEST),
		Middle_North_East(EnumFacing.NORTH, EnumFacing.EAST),
		Lower_South_East(EnumFacing.SOUTH, EnumFacing.EAST),
		Lower_South_West(EnumFacing.SOUTH, EnumFacing.WEST);

		final EnumFacing part1;
		final EnumFacing part2;

		Edge(EnumFacing part1, EnumFacing part2) {
			this.part1 = part1;
			this.part2 = part2;
		}
	}

	enum UpDown {
		UP("U", EnumFacing.UP),
		DOWN("D", EnumFacing.DOWN);

		final String s;
		final EnumFacing dir;

		UpDown(String s, EnumFacing dir) {
			this.s = s;
			this.dir = dir;
		}
	}

	enum NorthSouth {
		NORTH("N", EnumFacing.NORTH),
		SOUTH("S", EnumFacing.SOUTH);

		final String s;
		final EnumFacing dir;

		NorthSouth(String s, EnumFacing dir) {
			this.s = s;
			this.dir = dir;
		}
	}

	enum EastWest {
		EAST("E", EnumFacing.EAST),
		WEST("W", EnumFacing.WEST);

		final String s;
		final EnumFacing dir;

		EastWest(String s, EnumFacing dir) {
			this.s = s;
			this.dir = dir;
		}
	}

	enum Corner {
		UP_NORTH_WEST(UpDown.UP, NorthSouth.NORTH, EastWest.WEST),
		UP_NORTH_EAST(UpDown.UP, NorthSouth.NORTH, EastWest.EAST),
		UP_SOUTH_WEST(UpDown.UP, NorthSouth.SOUTH, EastWest.WEST),
		UP_SOUTH_EAST(UpDown.UP, NorthSouth.SOUTH, EastWest.EAST),
		DOWN_NORTH_WEST(UpDown.DOWN, NorthSouth.NORTH, EastWest.WEST),
		DOWN_NORTH_EAST(UpDown.DOWN, NorthSouth.NORTH, EastWest.EAST),
		DOWN_SOUTH_WEST(UpDown.DOWN, NorthSouth.SOUTH, EastWest.WEST),
		DOWN_SOUTH_EAST(UpDown.DOWN, NorthSouth.SOUTH, EastWest.EAST);

		final UpDown ud;
		final NorthSouth ns;
		final EastWest ew;

		Corner(UpDown ud, NorthSouth ns, EastWest ew) {
			this.ud = ud;
			this.ns = ns;
			this.ew = ew;
		}
	}

	enum Turn {
		NORTH_SOUTH(EnumFacing.NORTH, EnumFacing.SOUTH),
		EAST_WEST(EnumFacing.EAST, EnumFacing.WEST),
		UP_DOWN(EnumFacing.UP, EnumFacing.DOWN);

		final EnumFacing dir1;
		final EnumFacing dir2;

		Turn(EnumFacing dir1, EnumFacing dir2) {
			this.dir1 = dir1;
			this.dir2 = dir2;
		}
	}

	enum Turn_Corner {
		UP_NORTH_WEST_TURN_NORTH_SOUTH(Corner.UP_NORTH_WEST, Turn.NORTH_SOUTH, 1),
		UP_NORTH_WEST_TURN_EAST_WEST(Corner.UP_NORTH_WEST, Turn.EAST_WEST, 14),
		UP_NORTH_WEST_TURN_UP_DOWN(Corner.UP_NORTH_WEST, Turn.UP_DOWN, 23),
		UP_NORTH_EAST_TURN_NORTH_SOUTH(Corner.UP_NORTH_EAST, Turn.NORTH_SOUTH, 2),
		UP_NORTH_EAST_TURN_EAST_WEST(Corner.UP_NORTH_EAST, Turn.EAST_WEST, 9),
		UP_NORTH_EAST_TURN_UP_DOWN(Corner.UP_NORTH_EAST, Turn.UP_DOWN, 22),
		UP_SOUTH_WEST_TURN_NORTH_SOUTH(Corner.UP_SOUTH_WEST, Turn.NORTH_SOUTH, 6),
		UP_SOUTH_WEST_TURN_EAST_WEST(Corner.UP_SOUTH_WEST, Turn.EAST_WEST, 13),
		UP_SOUTH_WEST_TURN_UP_DOWN(Corner.UP_SOUTH_WEST, Turn.UP_DOWN, 24),
		UP_SOUTH_EAST_TURN_NORTH_SOUTH(Corner.UP_SOUTH_EAST, Turn.NORTH_SOUTH, 5),
		UP_SOUTH_EAST_TURN_EAST_WEST(Corner.UP_SOUTH_EAST, Turn.EAST_WEST, 10),
		UP_SOUTH_EAST_TURN_UP_DOWN(Corner.UP_SOUTH_EAST, Turn.UP_DOWN, 21),
		DOWN_NORTH_WEST_TURN_NORTH_SOUTH(Corner.DOWN_NORTH_WEST, Turn.NORTH_SOUTH, 4),
		DOWN_NORTH_WEST_TURN_EAST_WEST(Corner.DOWN_NORTH_WEST, Turn.EAST_WEST, 15),
		DOWN_NORTH_WEST_TURN_UP_DOWN(Corner.DOWN_NORTH_WEST, Turn.UP_DOWN, 20),
		DOWN_NORTH_EAST_TURN_NORTH_SOUTH(Corner.DOWN_NORTH_EAST, Turn.NORTH_SOUTH, 3),
		DOWN_NORTH_EAST_TURN_EAST_WEST(Corner.DOWN_NORTH_EAST, Turn.EAST_WEST, 12),
		DOWN_NORTH_EAST_TURN_UP_DOWN(Corner.DOWN_NORTH_EAST, Turn.UP_DOWN, 17),
		DOWN_SOUTH_WEST_TURN_NORTH_SOUTH(Corner.DOWN_SOUTH_WEST, Turn.NORTH_SOUTH, 7),
		DOWN_SOUTH_WEST_TURN_EAST_WEST(Corner.DOWN_SOUTH_WEST, Turn.EAST_WEST, 16),
		DOWN_SOUTH_WEST_TURN_UP_DOWN(Corner.DOWN_SOUTH_WEST, Turn.UP_DOWN, 19),
		DOWN_SOUTH_EAST_TURN_NORTH_SOUTH(Corner.DOWN_SOUTH_EAST, Turn.NORTH_SOUTH, 8),
		DOWN_SOUTH_EAST_TURN_EAST_WEST(Corner.DOWN_SOUTH_EAST, Turn.EAST_WEST, 11),
		DOWN_SOUTH_EAST_TURN_UP_DOWN(Corner.DOWN_SOUTH_EAST, Turn.UP_DOWN, 18);

		final Corner corner;
		final Turn turn;
		final int number;

		Turn_Corner(Corner corner, Turn turn, int number) {
			this.corner = corner;
			this.turn = turn;
			this.number = number;
		}

		public EnumFacing getPointer() {
			List<EnumFacing> canidates = new ArrayList<EnumFacing>();
			canidates.add(corner.ew.dir);
			canidates.add(corner.ns.dir);
			canidates.add(corner.ud.dir);
			if (canidates.contains(turn.dir1)) {
				return turn.dir1;
			} else if (canidates.contains(turn.dir2)) {
				return turn.dir2;
			} else {
				throw new UnsupportedOperationException(name());
			}
		}
	}

	enum SupportOri {
		UP_DOWN("U"),
		SIDE("S");

		final String s;

		SupportOri(String s) {
			this.s = s;
		}
	}

	enum Support {
		UP_UP(EnumFacing.UP, SupportOri.UP_DOWN),
		UP_SIDE(EnumFacing.UP, SupportOri.SIDE),
		DOWN_UP(EnumFacing.DOWN, SupportOri.UP_DOWN),
		DOWN_SIDE(EnumFacing.DOWN, SupportOri.SIDE),
		NORTH_UP(EnumFacing.NORTH, SupportOri.UP_DOWN),
		NORTH_SIDE(EnumFacing.NORTH, SupportOri.SIDE),
		SOUTH_UP(EnumFacing.SOUTH, SupportOri.UP_DOWN),
		SOUTH_SIDE(EnumFacing.SOUTH, SupportOri.SIDE),
		EAST_UP(EnumFacing.EAST, SupportOri.UP_DOWN),
		EAST_SIDE(EnumFacing.EAST, SupportOri.SIDE),
		WEST_UP(EnumFacing.WEST, SupportOri.UP_DOWN),
		WEST_SIDE(EnumFacing.WEST, SupportOri.SIDE);

		Support(EnumFacing dir, SupportOri ori) {
			this.dir = dir;
			this.ori = ori;
		}

		final EnumFacing dir;
		final SupportOri ori;
	}

	enum Mount {
		UP_NORTH(EnumFacing.UP, EnumFacing.NORTH),
		UP_SOUTH(EnumFacing.UP, EnumFacing.SOUTH),
		UP_EAST(EnumFacing.UP, EnumFacing.EAST),
		UP_WEST(EnumFacing.UP, EnumFacing.WEST),
		DOWN_NORTH(EnumFacing.DOWN, EnumFacing.NORTH),
		DOWN_SOUTH(EnumFacing.DOWN, EnumFacing.SOUTH),
		DOWN_EAST(EnumFacing.DOWN, EnumFacing.EAST),
		DOWN_WEST(EnumFacing.DOWN, EnumFacing.WEST),
		NORTH_UP(EnumFacing.NORTH, EnumFacing.UP),
		NORTH_DOWN(EnumFacing.NORTH, EnumFacing.DOWN),
		NORTH_EAST(EnumFacing.NORTH, EnumFacing.EAST),
		NORTH_WEST(EnumFacing.NORTH, EnumFacing.WEST),
		SOUTH_UP(EnumFacing.SOUTH, EnumFacing.UP),
		SOUTH_DOWN(EnumFacing.SOUTH, EnumFacing.DOWN),
		SOUTH_EAST(EnumFacing.SOUTH, EnumFacing.EAST),
		SOUTH_WEST(EnumFacing.SOUTH, EnumFacing.WEST),
		EAST_UP(EnumFacing.EAST, EnumFacing.UP),
		EAST_DOWN(EnumFacing.EAST, EnumFacing.DOWN),
		EAST_NORTH(EnumFacing.EAST, EnumFacing.NORTH),
		EAST_SOUTH(EnumFacing.EAST, EnumFacing.SOUTH),
		WEST_UP(EnumFacing.WEST, EnumFacing.UP),
		WEST_DOWN(EnumFacing.WEST, EnumFacing.DOWN),
		WEST_NORTH(EnumFacing.WEST, EnumFacing.NORTH),
		WEST_SOUTH(EnumFacing.WEST, EnumFacing.SOUTH);

		EnumFacing dir;
		EnumFacing side;

		Mount(EnumFacing dir, EnumFacing side) {
			this.dir = dir;
			this.side = side;
		}
	}

	static Map<EnumFacing, List<IModel3D>> sideNormal = new HashMap<EnumFacing, List<IModel3D>>();
	static Map<EnumFacing, List<IModel3D>> sideBC = new HashMap<EnumFacing, List<IModel3D>>();
	static Map<Edge, IModel3D> edges = new HashMap<Edge, IModel3D>();
	static Map<Corner, List<IModel3D>> corners_M = new HashMap<Corner, List<IModel3D>>();
	static Map<Corner, List<IModel3D>> corners_I3 = new HashMap<Corner, List<IModel3D>>();
	static Map<Turn_Corner, IModel3D> corners_I = new HashMap<Turn_Corner, IModel3D>();
	static Map<Support, IModel3D> supports = new HashMap<Support, IModel3D>();
	static Map<Turn_Corner, IModel3D> spacers = new HashMap<Turn_Corner, IModel3D>();
	static Map<Mount, IModel3D> mounts = new HashMap<Mount, IModel3D>();

	static Map<EnumFacing, List<IModel3D>> texturePlate_Inner = new HashMap<EnumFacing, List<IModel3D>>();
	static Map<EnumFacing, List<IModel3D>> texturePlate_Outer = new HashMap<EnumFacing, List<IModel3D>>();
	static Map<EnumFacing, Quartet<List<IModel3D>, List<IModel3D>, List<IModel3D>, List<IModel3D>>> sideTexturePlate = new HashMap<EnumFacing, Quartet<List<IModel3D>, List<IModel3D>, List<IModel3D>, List<IModel3D>>>();
	static Map<Mount, List<IModel3D>> textureConnectorPlate = new HashMap<Mount, List<IModel3D>>();
	static Map<Edge, Quartet<IModel3D, IModel3D, IModel3D, IModel3D>> centerEdgeLEDs = new HashMap<Edge, Quartet<IModel3D, IModel3D, IModel3D, IModel3D>>();
	static Map<EnumFacing, List<IModel3D>> sidedInnerLEDs = new HashMap<EnumFacing, List<IModel3D>>();
	static Map<EnumFacing, List<IModel3D>> sidedOuterLEDs = new HashMap<EnumFacing, List<IModel3D>>();

	static Map<ScaleObject, IModel3D> scaleMap = new HashMap<ScaleObject, IModel3D>();

	@Data
	@AllArgsConstructor
	private static class ScaleObject {

		private final IModel3D original;
		private final double scale;
	}

	static IModel3D innerTransportBox;

	public static IIconTransformation basicTexture;
	public static IIconTransformation inactiveTexture;
	public static IIconTransformation glassCenterTexture;
	public static IIconTransformation innerBoxTexture;
	public static IIconTransformation statusTexture;
	public static IIconTransformation statusBCTexture;

	private static final ResourceLocation BLOCKS = new ResourceLocation("textures/atlas/blocks.png");

	static {
		LogisticsNewRenderPipe.loadModels();
	}

	public static void loadModels() {
		if (!SimpleServiceLocator.cclProxy.isActivated()) return;
		try {
			Map<String, IModel3D> pipePartModels = SimpleServiceLocator.cclProxy.parseObjModels(LogisticsPipes.class.getResourceAsStream("/logisticspipes/models/PipeModel_result.obj"), 7, new LPScale(1 / 100f));

			for (EnumFacing dir : UtilEnumFacing.VALID_DIRECTIONS) {
				LogisticsNewRenderPipe.sideNormal.put(dir, new ArrayList<IModel3D>());
				String grp = "Side_" + LogisticsNewRenderPipe.getDirAsString_Type1(dir);
				for (Entry<String, IModel3D> entry : pipePartModels.entrySet()) {
					if (entry.getKey().contains(" " + grp + " ") || entry.getKey().endsWith(" " + grp)) {
						LogisticsNewRenderPipe.sideNormal.get(dir).add(LogisticsNewRenderPipe.compute(entry.getValue().backfacedCopy().apply(new LPTranslation(0.0, 0.0, 1.0))));
					}
				}
				if (LogisticsNewRenderPipe.sideNormal.get(dir).size() != 4) {
					throw new RuntimeException("Couldn't load " + dir.name() + " (" + grp + "). Only loaded " + LogisticsNewRenderPipe.sideNormal.get(dir).size());
				}
			}

			for (EnumFacing dir : UtilEnumFacing.VALID_DIRECTIONS) {
				LogisticsNewRenderPipe.sideBC.put(dir, new ArrayList<IModel3D>());
				String grp = "Side_BC_" + LogisticsNewRenderPipe.getDirAsString_Type1(dir);
				for (Entry<String, IModel3D> entry : pipePartModels.entrySet()) {
					if (entry.getKey().contains(" " + grp + " ") || entry.getKey().endsWith(" " + grp)) {
						LogisticsNewRenderPipe.sideBC.get(dir).add(LogisticsNewRenderPipe.compute(entry.getValue().backfacedCopy().apply(new LPTranslation(0.0, 0.0, 1.0))));
					}
				}
				if (LogisticsNewRenderPipe.sideBC.get(dir).size() != 8) {
					throw new RuntimeException("Couldn't load " + dir.name() + " (" + grp + "). Only loaded " + LogisticsNewRenderPipe.sideBC.get(dir).size());
				}
			}

			for (Edge edge : Edge.values()) {
				String grp;
				if (edge.part1 == EnumFacing.UP || edge.part1 == EnumFacing.DOWN) {
					grp = "Edge_M_" + LogisticsNewRenderPipe.getDirAsString_Type1(edge.part1) + "_" + LogisticsNewRenderPipe.getDirAsString_Type1(edge.part2);
				} else {
					grp = "Edge_M_S_" + LogisticsNewRenderPipe.getDirAsString_Type1(edge.part1) + LogisticsNewRenderPipe.getDirAsString_Type1(edge.part2);
				}
				for (Entry<String, IModel3D> entry : pipePartModels.entrySet()) {
					if (entry.getKey().contains(" " + grp + " ") || entry.getKey().endsWith(" " + grp)) {
						LogisticsNewRenderPipe.edges.put(edge, LogisticsNewRenderPipe.compute(entry.getValue().backfacedCopy().apply(new LPTranslation(0.0, 0.0, 1.0))));
						break;
					}
				}
				if (LogisticsNewRenderPipe.edges.get(edge) == null) {
					throw new RuntimeException("Couldn't load " + edge.name() + " (" + grp + ")");
				}
			}

			for (Corner corner : Corner.values()) {
				LogisticsNewRenderPipe.corners_M.put(corner, new ArrayList<IModel3D>());
				String grp = "Corner_M_" + corner.ud.s + "_" + corner.ns.s + corner.ew.s;
				for (Entry<String, IModel3D> entry : pipePartModels.entrySet()) {
					if (entry.getKey().contains(" " + grp + " ") || entry.getKey().endsWith(" " + grp)) {
						LogisticsNewRenderPipe.corners_M.get(corner).add(LogisticsNewRenderPipe.compute(entry.getValue().backfacedCopy().apply(new LPTranslation(0.0, 0.0, 1.0))));
					}
				}
				if (LogisticsNewRenderPipe.corners_M.get(corner).size() != 2) {
					throw new RuntimeException("Couldn't load " + corner.name() + " (" + grp + "). Only loaded " + LogisticsNewRenderPipe.corners_M.get(corner).size());
				}
			}

			for (Corner corner : Corner.values()) {
				LogisticsNewRenderPipe.corners_I3.put(corner, new ArrayList<IModel3D>());
				String grp = "Corner_I3_" + corner.ud.s + "_" + corner.ns.s + corner.ew.s;
				for (Entry<String, IModel3D> entry : pipePartModels.entrySet()) {
					if (entry.getKey().contains(" " + grp + " ") || entry.getKey().endsWith(" " + grp)) {
						LogisticsNewRenderPipe.corners_I3.get(corner).add(LogisticsNewRenderPipe.compute(entry.getValue().backfacedCopy().apply(new LPTranslation(0.0, 0.0, 1.0))));
					}
				}
				if (LogisticsNewRenderPipe.corners_I3.get(corner).size() != 2) {
					throw new RuntimeException("Couldn't load " + corner.name() + " (" + grp + "). Only loaded " + LogisticsNewRenderPipe.corners_I3.get(corner).size());
				}
			}

			for (Support support : Support.values()) {
				String grp = "Support_" + LogisticsNewRenderPipe.getDirAsString_Type1(support.dir) + "_" + support.ori.s;
				for (Entry<String, IModel3D> entry : pipePartModels.entrySet()) {
					if (entry.getKey().contains(" " + grp + " ") || entry.getKey().endsWith(" " + grp)) {
						LogisticsNewRenderPipe.supports.put(support, LogisticsNewRenderPipe.compute(entry.getValue().backfacedCopy().apply(new LPTranslation(0.0, 0.0, 1.0))));
						break;
					}
				}
				if (LogisticsNewRenderPipe.supports.get(support) == null) {
					throw new RuntimeException("Couldn't load " + support.name() + " (" + grp + ")");
				}
			}

			for (Turn_Corner corner : Turn_Corner.values()) {
				String grp = "Corner_I_" + corner.corner.ud.s + "_" + corner.corner.ns.s + corner.corner.ew.s;
				for (Entry<String, IModel3D> entry : pipePartModels.entrySet()) {
					if (entry.getKey().contains(" " + grp)) {
						char c = ' ';
						if (!entry.getKey().endsWith(" " + grp)) {
							c = entry.getKey().charAt(entry.getKey().indexOf(" " + grp) + (" " + grp).length());
						}
						if (Character.isDigit(c)) {
							if (c == '2') {
								if (corner.turn != Turn.NORTH_SOUTH) {
									continue;
								}
							} else if (c == '1') {
								if (corner.turn != Turn.EAST_WEST) {
									continue;
								}
							} else {
								throw new UnsupportedOperationException();
							}
						} else {
							if (corner.turn != Turn.UP_DOWN) {
								continue;
							}
						}
						LogisticsNewRenderPipe.corners_I.put(corner, LogisticsNewRenderPipe.compute(entry.getValue().backfacedCopy().apply(new LPTranslation(0.0, 0.0, 1.0))));
						break;
					}
				}
				if (LogisticsNewRenderPipe.corners_I.get(corner) == null) {
					throw new RuntimeException("Couldn't load " + corner.name() + " (" + grp + ")");
				}
			}

			for (Turn_Corner corner : Turn_Corner.values()) {
				String grp = "Spacer" + corner.number;
				for (Entry<String, IModel3D> entry : pipePartModels.entrySet()) {
					if (entry.getKey().contains(" " + grp + " ") || entry.getKey().endsWith(" " + grp)) {
						LogisticsNewRenderPipe.spacers.put(corner, LogisticsNewRenderPipe.compute(entry.getValue().backfacedCopy().apply(new LPTranslation(0.0, 0.0, 1.0))));
						break;
					}
				}
				if (LogisticsNewRenderPipe.spacers.get(corner) == null) {
					throw new RuntimeException("Couldn't load " + corner.name() + " (" + grp + ")");
				}
			}

			for (Mount mount : Mount.values()) {
				String grp = "Mount_" + LogisticsNewRenderPipe.getDirAsString_Type1(mount.dir) + "_" + LogisticsNewRenderPipe.getDirAsString_Type1(mount.side);
				for (Entry<String, IModel3D> entry : pipePartModels.entrySet()) {
					if (entry.getKey().contains(" " + grp + " ") || entry.getKey().endsWith(" " + grp)) {
						LogisticsNewRenderPipe.mounts.put(mount, LogisticsNewRenderPipe.compute(entry.getValue().backfacedCopy().apply(new LPTranslation(0.0, 0.0, 1.0))));
						break;
					}
				}
				if (LogisticsNewRenderPipe.mounts.get(mount) == null) {
					throw new RuntimeException("Couldn't load " + mount.name() + " (" + grp + ")");
				}
			}

			for (EnumFacing dir : UtilEnumFacing.VALID_DIRECTIONS) {
				LogisticsNewRenderPipe.texturePlate_Inner.put(dir, new ArrayList<IModel3D>());
				String grp = "Inner_Plate_" + LogisticsNewRenderPipe.getDirAsString_Type1(dir);
				for (Entry<String, IModel3D> entry : pipePartModels.entrySet()) {
					if (entry.getKey().contains(" " + grp)) {
						LogisticsNewRenderPipe.texturePlate_Inner.get(dir).add(LogisticsNewRenderPipe.compute(entry.getValue().backfacedCopy().apply(new LPTranslation(0.0, 0.0, 1.0))));
					}
				}
				if (LogisticsNewRenderPipe.texturePlate_Inner.get(dir).size() != 2) {
					throw new RuntimeException("Couldn't load " + dir.name() + " (" + grp + "). Only loaded " + LogisticsNewRenderPipe.texturePlate_Inner.get(dir).size());
				}
			}

			for (EnumFacing dir : UtilEnumFacing.VALID_DIRECTIONS) {
				LogisticsNewRenderPipe.texturePlate_Outer.put(dir, new ArrayList<IModel3D>());
				String grp = "Texture_Plate_" + LogisticsNewRenderPipe.getDirAsString_Type1(dir);
				for (Entry<String, IModel3D> entry : pipePartModels.entrySet()) {
					if (entry.getKey().contains(" " + grp)) {
						LogisticsNewRenderPipe.texturePlate_Outer.get(dir).add(LogisticsNewRenderPipe.compute(entry.getValue().backfacedCopy().apply(new LPTranslation(0.0, 0.0, 1.0)).apply(new LPTranslation(-0.5, -0.5, -0.5)).apply(new LPScale(1.001D)).apply(new LPTranslation(0.5, 0.5, 0.5))));
					}
				}
				if (LogisticsNewRenderPipe.texturePlate_Outer.get(dir).size() != 2) {
					throw new RuntimeException("Couldn't load " + dir.name() + " (" + grp + "). Only loaded " + LogisticsNewRenderPipe.texturePlate_Outer.get(dir).size());
				}
			}

			for (EnumFacing dir : UtilEnumFacing.VALID_DIRECTIONS) {
				LogisticsNewRenderPipe.sideTexturePlate.put(dir, new Quartet<List<IModel3D>, List<IModel3D>, List<IModel3D>, List<IModel3D>>(new ArrayList<IModel3D>(), new ArrayList<IModel3D>(), new ArrayList<IModel3D>(), new ArrayList<IModel3D>()));
				String grp = "Texture_Side_" + LogisticsNewRenderPipe.getDirAsString_Type1(dir);
				for (Entry<String, IModel3D> entry : pipePartModels.entrySet()) {
					if (entry.getKey().contains(" " + grp)) {
						IModel3D model = LogisticsNewRenderPipe.compute(entry.getValue().backfacedCopy().apply(new LPTranslation(0.0, 0.0, 1.0)));
						double sizeA = (model.bounds().max().x() - model.bounds().min().x()) + (model.bounds().max().y() - model.bounds().min().y()) + (model.bounds().max().z() - model.bounds().min().z());
						double dis = Math.pow(model.bounds().min().x() - 0.5D, 2) + Math.pow(model.bounds().min().y() - 0.5D, 2) + Math.pow(model.bounds().min().z() - 0.5D, 2);
						if (sizeA < 0.5D) {
							if ((dis > 0.21 && dis < 0.23) || (dis > 0.37 && dis < 0.39)) {
								LogisticsNewRenderPipe.sideTexturePlate.get(dir).getValue4().add(model);
							} else if ((dis < 0.2 && dis > 0.18) || (dis < 0.36 && dis > 0.34)) {
								LogisticsNewRenderPipe.sideTexturePlate.get(dir).getValue2().add(model);
							} else {
								throw new UnsupportedOperationException("Dis: " + dis);
							}
						} else {
							if ((dis > 0.21 && dis < 0.23) || (dis > 0.37 && dis < 0.39)) {
								LogisticsNewRenderPipe.sideTexturePlate.get(dir).getValue3().add(model);
							} else if ((dis < 0.2 && dis > 0.18) || (dis < 0.36 && dis > 0.34)) {
								LogisticsNewRenderPipe.sideTexturePlate.get(dir).getValue1().add(model);
							} else {
								throw new UnsupportedOperationException("Dis: " + dis);
							}
						}
					}
				}
				if (LogisticsNewRenderPipe.sideTexturePlate.get(dir).getValue1().size() != 8) {
					throw new RuntimeException("Couldn't load " + dir.name() + " (" + grp + "). Only loaded " + LogisticsNewRenderPipe.sideTexturePlate.get(dir).getValue1().size());
				}
				if (LogisticsNewRenderPipe.sideTexturePlate.get(dir).getValue2().size() != 8) {
					throw new RuntimeException("Couldn't load " + dir.name() + " (" + grp + "). Only loaded " + LogisticsNewRenderPipe.sideTexturePlate.get(dir).getValue2().size());
				}
				if (LogisticsNewRenderPipe.sideTexturePlate.get(dir).getValue3().size() != 8) {
					throw new RuntimeException("Couldn't load " + dir.name() + " (" + grp + "). Only loaded " + LogisticsNewRenderPipe.sideTexturePlate.get(dir).getValue3().size());
				}
				if (LogisticsNewRenderPipe.sideTexturePlate.get(dir).getValue4().size() != 8) {
					throw new RuntimeException("Couldn't load " + dir.name() + " (" + grp + "). Only loaded " + LogisticsNewRenderPipe.sideTexturePlate.get(dir).getValue4().size());
				}
			}

			for (Mount mount : Mount.values()) {
				LogisticsNewRenderPipe.textureConnectorPlate.put(mount, new ArrayList<IModel3D>());
				String grp = "Texture_Connector_" + LogisticsNewRenderPipe.getDirAsString_Type1(mount.dir) + "_" + LogisticsNewRenderPipe.getDirAsString_Type1(mount.side);
				for (Entry<String, IModel3D> entry : pipePartModels.entrySet()) {
					if (entry.getKey().contains(" " + grp + " ") || entry.getKey().endsWith(" " + grp)) {
						LogisticsNewRenderPipe.textureConnectorPlate.get(mount).add(LogisticsNewRenderPipe.compute(entry.getValue().backfacedCopy().apply(new LPTranslation(0.0, 0.0, 1.0))));
					}
				}
				if (LogisticsNewRenderPipe.textureConnectorPlate.get(mount).size() != 4) {
					throw new RuntimeException("Couldn't load " + mount.name() + " (" + grp + "). Only loaded " + LogisticsNewRenderPipe.textureConnectorPlate.get(mount).size());
				}
			}

			for (Edge edge : Edge.values()) {
				LogisticsNewRenderPipe.centerEdgeLEDs.put(edge, new Quartet<IModel3D, IModel3D, IModel3D, IModel3D>(null, null, null, null));
				for (int i = 0; i < 4; i++) {
					String grp = "Center_LED_" + (i + 1) + "_" + LogisticsNewRenderPipe.getDirAsString_Type1(edge.part1) + LogisticsNewRenderPipe.getDirAsString_Type1(edge.part2);
					for (Entry<String, IModel3D> entry : pipePartModels.entrySet()) {
						if (entry.getKey().contains(" " + grp + " ") || entry.getKey().endsWith(" " + grp)) {
							IModel3D model = LogisticsNewRenderPipe.compute(entry.getValue().backfacedCopy().apply(new LPTranslation(0.0, 0.0, 1.0)).apply(new LPTranslation(-0.5, -0.5, -0.5)).apply(new LPScale(1.001D)).apply(new LPTranslation(0.5, 0.5, 0.5)));
							if (i == 0) {
								LogisticsNewRenderPipe.centerEdgeLEDs.get(edge).setValue1(model);
							}
							if (i == 1) {
								LogisticsNewRenderPipe.centerEdgeLEDs.get(edge).setValue2(model);
							}
							if (i == 2) {
								LogisticsNewRenderPipe.centerEdgeLEDs.get(edge).setValue3(model);
							}
							if (i == 3) {
								LogisticsNewRenderPipe.centerEdgeLEDs.get(edge).setValue4(model);
							}
							break;
						}
					}
				}
				if (LogisticsNewRenderPipe.centerEdgeLEDs.get(edge).getValue1() == null || LogisticsNewRenderPipe.centerEdgeLEDs.get(edge).getValue2() == null || LogisticsNewRenderPipe.centerEdgeLEDs.get(edge).getValue3() == null || LogisticsNewRenderPipe.centerEdgeLEDs.get(edge).getValue4() == null) {
					throw new RuntimeException("Couldn't load " + edge.name());
				}
			}

			for (EnumFacing dir : UtilEnumFacing.VALID_DIRECTIONS) {
				LogisticsNewRenderPipe.sidedInnerLEDs.put(dir, new ArrayList<IModel3D>());
				String grp = "Inner_LED_" + LogisticsNewRenderPipe.getDirAsString_Type1(dir);
				for (Entry<String, IModel3D> entry : pipePartModels.entrySet()) {
					if (entry.getKey().contains(" " + grp)) {
						LogisticsNewRenderPipe.sidedInnerLEDs.get(dir).add(LogisticsNewRenderPipe.compute(entry.getValue().backfacedCopy().apply(new LPTranslation(0.0, 0.0, 1.0)).apply(new LPTranslation(-0.5, -0.5, -0.5)).apply(new LPScale(1.001D)).apply(new LPTranslation(0.5, 0.5, 0.5))));
					}
				}
				if (LogisticsNewRenderPipe.sidedInnerLEDs.get(dir).size() != 4) {
					throw new RuntimeException("Couldn't load " + dir.name() + " (" + grp + "). Only loaded " + LogisticsNewRenderPipe.sidedInnerLEDs.get(dir).size());
				}
			}

			for (EnumFacing dir : UtilEnumFacing.VALID_DIRECTIONS) {
				LogisticsNewRenderPipe.sidedOuterLEDs.put(dir, new ArrayList<IModel3D>());
				String grp = "Outer_LED_" + LogisticsNewRenderPipe.getDirAsString_Type1(dir);
				for (Entry<String, IModel3D> entry : pipePartModels.entrySet()) {
					if (entry.getKey().contains(" " + grp)) {
						LogisticsNewRenderPipe.sidedOuterLEDs.get(dir).add(LogisticsNewRenderPipe.compute(entry.getValue().backfacedCopy().apply(new LPTranslation(0.0, 0.0, 1.0)).apply(new LPTranslation(-0.5, -0.5, -0.5)).apply(new LPScale(1.001D)).apply(new LPTranslation(0.5, 0.5, 0.5))));
					}
				}
				if (LogisticsNewRenderPipe.sidedOuterLEDs.get(dir).size() != 4) {
					throw new RuntimeException("Couldn't load " + dir.name() + " (" + grp + "). Only loaded " + LogisticsNewRenderPipe.sidedOuterLEDs.get(dir).size());
				}
			}

			pipePartModels = SimpleServiceLocator.cclProxy.parseObjModels(LogisticsPipes.class.getResourceAsStream("/logisticspipes/models/PipeModel_Transport_Box.obj"), 7, new LPScale(1 / 100f));

			LogisticsNewRenderPipe.innerTransportBox = LogisticsNewRenderPipe.compute(pipePartModels.get("InnerTransportBox").backfacedCopy().apply(new LPTranslation(0.0, 0.0, 1.0)).apply(new LPTranslation(-0.5, -0.5, -0.5)).apply(new LPScale(0.99D)).apply(new LPTranslation(0.5, 0.5, 0.5)));

		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	private static String getDirAsString_Type1(EnumFacing dir) {
		switch (dir) {
			case NORTH:
				return "N";
			case SOUTH:
				return "S";
			case EAST:
				return "E";
			case WEST:
				return "W";
			case UP:
				return "U";
			case DOWN:
				return "D";
			default:
				return "UNKNWON";
		}
	}

	private static IModel3D compute(IModel3D m) {
		m.computeNormals();
		//m.computeLighting(LightModel.standardLightModel);
		m.computeStandardLighting();
		return m;
	}

	public static void registerTextures(IIconRegister iconRegister) {
		if (LogisticsNewRenderPipe.basicTexture == null) {
			LogisticsNewRenderPipe.basicTexture = SimpleServiceLocator.cclProxy.createIconTransformer(iconRegister.registerIcon("logisticspipes:" + "pipes/PipeModel"));
			LogisticsNewRenderPipe.inactiveTexture = SimpleServiceLocator.cclProxy.createIconTransformer(iconRegister.registerIcon("logisticspipes:" + "pipes/PipeModel-inactive"));
			LogisticsNewRenderPipe.innerBoxTexture = SimpleServiceLocator.cclProxy.createIconTransformer(iconRegister.registerIcon("logisticspipes:" + "pipes/InnerBox"));
			LogisticsNewRenderPipe.glassCenterTexture = SimpleServiceLocator.cclProxy.createIconTransformer(iconRegister.registerIcon("logisticspipes:" + "pipes/Glass_Texture_Center"));
			LogisticsNewRenderPipe.statusTexture = SimpleServiceLocator.cclProxy.createIconTransformer(iconRegister.registerIcon("logisticspipes:" + "pipes/PipeModel-status"));
			LogisticsNewRenderPipe.statusBCTexture = SimpleServiceLocator.cclProxy.createIconTransformer(iconRegister.registerIcon("logisticspipes:" + "pipes/PipeModel-status-BC"));
		} else {
			LogisticsNewRenderPipe.basicTexture.update(iconRegister.registerIcon("logisticspipes:" + "pipes/PipeModel"));
			LogisticsNewRenderPipe.inactiveTexture.update(iconRegister.registerIcon("logisticspipes:" + "pipes/PipeModel-inactive"));
			LogisticsNewRenderPipe.innerBoxTexture.update(iconRegister.registerIcon("logisticspipes:" + "pipes/InnerBox"));
			LogisticsNewRenderPipe.glassCenterTexture.update(iconRegister.registerIcon("logisticspipes:" + "pipes/Glass_Texture_Center"));
			LogisticsNewRenderPipe.statusTexture.update(iconRegister.registerIcon("logisticspipes:" + "pipes/PipeModel-status"));
			LogisticsNewRenderPipe.statusBCTexture.update(iconRegister.registerIcon("logisticspipes:" + "pipes/PipeModel-status-BC"));
		}
	}

	private PlayerConfig config = LogisticsPipes.getClientPlayerConfig();

	public void renderTileEntityAt(LogisticsTileGenericPipe pipeTile, double x, double y, double z, float partialTickTime, double distance) {
		if (pipeTile.pipe instanceof PipeBlockRequestTable) {
			return;
		}

		Minecraft.getMinecraft().getTextureManager().bindTexture(LogisticsNewRenderPipe.BLOCKS);
		PipeRenderState renderState = pipeTile.renderState;

		if (renderState.renderList != null && renderState.renderList.isInvalid()) {
			renderState.renderList = null;
		}

		if (renderState.renderList == null) {
			renderState.renderList = SimpleServiceLocator.renderListHandler.getNewRenderList();
		}

		if (distance > config.getRenderPipeDistance() * config.getRenderPipeDistance()) {
			if (config.isUseFallbackRenderer()) {
				renderState.forceRenderOldPipe = true;
			}
		} else {
			renderState.forceRenderOldPipe = false;
			boolean recalculateList = false;
			if (renderState.cachedRenderer == null) {
				List<Pair<IModel3D, I3DOperation[]>> objectsToRender = new ArrayList<Pair<IModel3D, I3DOperation[]>>();
				fillObjectsToRenderList(objectsToRender, pipeTile, renderState);
				renderState.cachedRenderer = objectsToRender;
				recalculateList = true;
			}
			if (!renderState.renderList.isFilled() || recalculateList) {
				renderState.renderList.startListCompile();

				Tessellator tess = Tessellator.instance;

				SimpleServiceLocator.cclProxy.getRenderState().reset();
				SimpleServiceLocator.cclProxy.getRenderState().setUseNormals(true);
				SimpleServiceLocator.cclProxy.getRenderState().setAlphaOverride(0xff);

				int brightness = new LPPosition((TileEntity) pipeTile).getBlock(pipeTile.getWorld()).getMixedBrightnessForBlock(pipeTile.getWorld(), pipeTile.xCoord, pipeTile.yCoord, pipeTile.zCoord);

				tess.setColorOpaque_F(1F, 1F, 1F);
				tess.setBrightness(brightness);

				tess.startDrawingQuads();
				for (Pair<IModel3D, I3DOperation[]> model : renderState.cachedRenderer) {
					if (model == null) {
						SimpleServiceLocator.cclProxy.getRenderState().setAlphaOverride(0xa0);
					} else {
						model.getValue1().render(model.getValue2());
					}
				}
				SimpleServiceLocator.cclProxy.getRenderState().setAlphaOverride(0xff);
				tess.draw();

				renderState.renderList.stopCompile();
			}
			if (renderState.renderList != null) {
				GL11.glPushMatrix();
				GL11.glTranslated(x, y, z);
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ZERO);
				renderState.renderList.render();
				GL11.glDisable(GL11.GL_BLEND);
				GL11.glPopMatrix();
			}
		}
	}

	private void fillObjectsToRenderList(List<Pair<IModel3D, I3DOperation[]>> objectsToRender, LogisticsTileGenericPipe pipeTile, PipeRenderState renderState) {
		List<Edge> edgesToRender = new ArrayList<Edge>(Arrays.asList(Edge.values()));
		Map<Corner, Integer> connectionAtCorner = new HashMap<Corner, Integer>();
		List<Mount> mountCanidates = new ArrayList<Mount>(Arrays.asList(Mount.values()));

		int connectionCount = 0;

		for (EnumFacing dir : UtilEnumFacing.VALID_DIRECTIONS) {
			if (renderState.pipeConnectionMatrix.isConnected(dir)) {
				connectionCount++;
				if (renderState.pipeConnectionMatrix.isBCConnected(dir) || renderState.pipeConnectionMatrix.isTDConnected(dir)) {
					I3DOperation[] texture = new I3DOperation[] { LogisticsNewRenderPipe.basicTexture };
					if (renderState.textureMatrix.isRouted()) {
						if (renderState.textureMatrix.isRoutedInDir(dir)) {
							if (renderState.textureMatrix.isSubPowerInDir(dir)) {
								texture = new I3DOperation[] { new LPUVTransformationList(new LPUVTranslation(0, +23F / 100), LogisticsNewRenderPipe.statusBCTexture) };
							} else {
								texture = new I3DOperation[] { LogisticsNewRenderPipe.statusBCTexture };
							}
						} else {
							texture = new I3DOperation[] { new LPUVTransformationList(new LPUVTranslation(0, -23F / 100), LogisticsNewRenderPipe.statusBCTexture) };
						}
					}
					for (IModel3D model : LogisticsNewRenderPipe.sideBC.get(dir)) {
						objectsToRender.add(new Pair<IModel3D, I3DOperation[]>(model, texture));
					}
				} else {
					I3DOperation[] texture = new I3DOperation[] { LogisticsNewRenderPipe.basicTexture };
					if (renderState.textureMatrix.isRouted()) {
						if (renderState.textureMatrix.isRoutedInDir(dir)) {
							if (renderState.textureMatrix.isSubPowerInDir(dir)) {
								texture = new I3DOperation[] { new LPUVTransformationList(new LPUVTranslation(-2.5F / 10, 0), LogisticsNewRenderPipe.statusTexture) };
							} else {
								texture = new I3DOperation[] { LogisticsNewRenderPipe.statusTexture };
							}
						} else {
							if (renderState.textureMatrix.isHasPowerUpgrade()) {
								if (renderState.textureMatrix.getPointedOrientation() == dir) {
									texture = new I3DOperation[] { new LPUVTransformationList(new LPUVTranslation(+2.5F / 10, 0), LogisticsNewRenderPipe.statusTexture) };
								} else {
									texture = new I3DOperation[] { new LPUVTransformationList(new LPUVTranslation(-2.5F / 10, 37F / 100), LogisticsNewRenderPipe.statusTexture) };
								}
							} else {
								if (renderState.textureMatrix.getPointedOrientation() == dir) {
									texture = new I3DOperation[] { new LPUVTransformationList(new LPUVTranslation(+2.5F / 10, 37F / 100), LogisticsNewRenderPipe.statusTexture) };
								} else {
									texture = new I3DOperation[] { new LPUVTransformationList(new LPUVTranslation(0, 37F / 100), LogisticsNewRenderPipe.statusTexture) };
								}
							}
						}
					}
					for (IModel3D model : LogisticsNewRenderPipe.sideNormal.get(dir)) {
						Block block = new LPPosition((TileEntity) pipeTile).moveForward(dir).getBlock(pipeTile.getWorld());
						double[] bounds = { block.getBlockBoundsMinY(), block.getBlockBoundsMinZ(), block.getBlockBoundsMinX(), block.getBlockBoundsMaxY(), block.getBlockBoundsMaxZ(), block.getBlockBoundsMaxX() };
						double bound = bounds[dir.ordinal() / 2 + (dir.ordinal() % 2 == 0 ? 3 : 0)];
						ScaleObject key = new ScaleObject(model, bound);
						IModel3D model2 = LogisticsNewRenderPipe.scaleMap.get(key);
						if (model2 == null) {
							model2 = model.copy();
							IVec3 min = model2.bounds().min();
							model2.apply(new LPTranslation(min).inverse());
							double toAdd = 1;
							if (dir.ordinal() % 2 == 1) {
								toAdd = 1 + (bound / LPConstants.PIPE_MIN_POS);
								model2.apply(new LPScale(dir.getFrontOffsetX() != 0 ? toAdd : 1, dir.getFrontOffsetY() != 0 ? toAdd : 1, dir.getFrontOffsetZ() != 0 ? toAdd : 1));
							} else {
								bound = 1 - bound;
								toAdd = 1 + (bound / LPConstants.PIPE_MIN_POS);
								model2.apply(new LPScale(dir.getFrontOffsetX() != 0 ? toAdd : 1, dir.getFrontOffsetY() != 0 ? toAdd : 1, dir.getFrontOffsetZ() != 0 ? toAdd : 1));
								model2.apply(new LPTranslation(dir.getFrontOffsetX() * bound, dir.getFrontOffsetY() * bound, dir.getFrontOffsetZ() * bound));
							}
							model2.apply(new LPTranslation(min));
							LogisticsNewRenderPipe.scaleMap.put(key, model2);
						}
						objectsToRender.add(new Pair<IModel3D, I3DOperation[]>(model2, texture));
					}
					/*
					for(CCModel model: sidedInnerLEDs.get(dir)) {
						if(renderState.textureMatrix.getPointedOrientation() == dir) {
							if(!renderState.textureMatrix.isRoutedInDir(dir) && renderState.textureMatrix.isHasPowerUpgrade()) {
								objectsToRender.add(new Pair<CCModel, IVertexOperation[]>(model, new IVertexOperation[]{new UVTransformationList(new UVScale(1, 6F/8), orangeTexture)}));
								objectsToRender.add(new Pair<CCModel, IVertexOperation[]>(model, new IVertexOperation[]{new UVTransformationList(new UVScale(1, 8F/8), blueTexture)}));
							} else {
								objectsToRender.add(new Pair<CCModel, IVertexOperation[]>(model, new IVertexOperation[]{new UVTransformationList(new UVScale(1, 6F/8), orangeTexture)}));
							}
						} else if(renderState.textureMatrix.isSubPowerInDir(dir) || (!renderState.textureMatrix.isRoutedInDir(dir) && renderState.textureMatrix.isHasPowerUpgrade())) {
							objectsToRender.add(new Pair<CCModel, IVertexOperation[]>(model, new IVertexOperation[]{new UVTransformationList(new UVScale(1, 6F/8), blueTexture)}));
						}
					}
					for(CCModel model: sidedOuterLEDs.get(dir)) {
						if(renderState.textureMatrix.isRoutedInDir(dir)) {
							objectsToRender.add(new Pair<CCModel, IVertexOperation[]>(model, new IVertexOperation[]{new UVTransformationList(new UVScale(1, 6F/8), greenTexture)}));
						} else {
							objectsToRender.add(new Pair<CCModel, IVertexOperation[]>(model, new IVertexOperation[]{new UVTransformationList(new UVScale(1, 6F/8), redTexture)}));
						}
					}
					//*/
				}
				for (Edge edge : Edge.values()) {
					if (edge.part1 == dir || edge.part2 == dir) {
						edgesToRender.remove(edge);
						for (Mount mount : Mount.values()) {
							if ((mount.dir == edge.part1 && mount.side == edge.part2) || (mount.dir == edge.part2 && mount.side == edge.part1)) {
								mountCanidates.remove(mount);
							}
						}
					}
				}
				for (Corner corner : Corner.values()) {
					if (corner.ew.dir == dir || corner.ns.dir == dir || corner.ud.dir == dir) {
						if (!connectionAtCorner.containsKey(corner)) {
							connectionAtCorner.put(corner, 1);
						} else {
							connectionAtCorner.put(corner, connectionAtCorner.get(corner) + 1);
						}
					}
				}
			}
		}

		for (Corner corner : Corner.values()) {
			IIconTransformation cornerTexture = LogisticsNewRenderPipe.basicTexture;
			if (!renderState.textureMatrix.isHasPower() && renderState.textureMatrix.isRouted()) {
				cornerTexture = LogisticsNewRenderPipe.inactiveTexture;
			} else if (!renderState.textureMatrix.isRouted() && connectionCount > 2) {
				cornerTexture = LogisticsNewRenderPipe.inactiveTexture;
			}
			int count = connectionAtCorner.containsKey(corner) ? connectionAtCorner.get(corner) : 0;
			if (count == 0) {
				for (IModel3D model : LogisticsNewRenderPipe.corners_M.get(corner)) {
					objectsToRender.add(new Pair<IModel3D, I3DOperation[]>(model, new I3DOperation[] { cornerTexture }));
				}
			} else if (count == 1) {
				for (Turn_Corner turn : Turn_Corner.values()) {
					if (turn.corner != corner) {
						continue;
					}
					if (renderState.pipeConnectionMatrix.isConnected(turn.getPointer())) {
						objectsToRender.add(new Pair<IModel3D, I3DOperation[]>(LogisticsNewRenderPipe.spacers.get(turn), new I3DOperation[] { cornerTexture }));
						break;
					}
				}
			} else if (count == 2) {
				for (Turn_Corner turn : Turn_Corner.values()) {
					if (turn.corner != corner) {
						continue;
					}
					if (!renderState.pipeConnectionMatrix.isConnected(turn.getPointer())) {
						objectsToRender.add(new Pair<IModel3D, I3DOperation[]>(LogisticsNewRenderPipe.corners_I.get(turn), new I3DOperation[] { cornerTexture }));
						break;
					}
				}
			} else if (count == 3) {
				for (IModel3D model : LogisticsNewRenderPipe.corners_I3.get(corner)) {
					objectsToRender.add(new Pair<IModel3D, I3DOperation[]>(model, new I3DOperation[] { cornerTexture }));
				}
			}
		}

		for (Edge edge : edgesToRender) {
			objectsToRender.add(new Pair<IModel3D, I3DOperation[]>(LogisticsNewRenderPipe.edges.get(edge), new I3DOperation[] { LogisticsNewRenderPipe.basicTexture }));

			/*
			objectsToRender.add(new Pair<CCModel, IconTransformation>(centerEdgeLEDs.get(edge).getValue1(), activeTexture));
			objectsToRender.add(new Pair<CCModel, IconTransformation>(centerEdgeLEDs.get(edge).getValue2(), inactiveTexture));
			objectsToRender.add(new Pair<CCModel, IconTransformation>(centerEdgeLEDs.get(edge).getValue3(), inactiveTexture));
			objectsToRender.add(new Pair<CCModel, IconTransformation>(centerEdgeLEDs.get(edge).getValue4(), activeTexture));
			 */
		}

		for (int i = 0; i < 6; i += 2) {
			EnumFacing dir = UtilEnumFacing.getOrientation(i);
			List<EnumFacing> list = new ArrayList<EnumFacing>(Arrays.asList(UtilEnumFacing.VALID_DIRECTIONS));
			list.remove(dir);
			list.remove(dir.getOpposite());
			if (renderState.pipeConnectionMatrix.isConnected(dir) && renderState.pipeConnectionMatrix.isConnected(dir.getOpposite())) {
				boolean found = false;
				for (EnumFacing dir2 : list) {
					if (renderState.pipeConnectionMatrix.isConnected(dir2)) {
						found = true;
						break;
					}
				}
				if (!found) {
					switch (dir) {
						case DOWN:
							objectsToRender.add(new Pair<IModel3D, I3DOperation[]>(LogisticsNewRenderPipe.supports.get(Support.EAST_SIDE), new I3DOperation[] { LogisticsNewRenderPipe.basicTexture }));
							objectsToRender.add(new Pair<IModel3D, I3DOperation[]>(LogisticsNewRenderPipe.supports.get(Support.WEST_SIDE), new I3DOperation[] { LogisticsNewRenderPipe.basicTexture }));
							objectsToRender.add(new Pair<IModel3D, I3DOperation[]>(LogisticsNewRenderPipe.supports.get(Support.NORTH_SIDE), new I3DOperation[] { LogisticsNewRenderPipe.basicTexture }));
							objectsToRender.add(new Pair<IModel3D, I3DOperation[]>(LogisticsNewRenderPipe.supports.get(Support.SOUTH_SIDE), new I3DOperation[] { LogisticsNewRenderPipe.basicTexture }));
							break;
						case NORTH:
							objectsToRender.add(new Pair<IModel3D, I3DOperation[]>(LogisticsNewRenderPipe.supports.get(Support.EAST_UP), new I3DOperation[] { LogisticsNewRenderPipe.basicTexture }));
							objectsToRender.add(new Pair<IModel3D, I3DOperation[]>(LogisticsNewRenderPipe.supports.get(Support.WEST_UP), new I3DOperation[] { LogisticsNewRenderPipe.basicTexture }));
							objectsToRender.add(new Pair<IModel3D, I3DOperation[]>(LogisticsNewRenderPipe.supports.get(Support.UP_SIDE), new I3DOperation[] { LogisticsNewRenderPipe.basicTexture }));
							objectsToRender.add(new Pair<IModel3D, I3DOperation[]>(LogisticsNewRenderPipe.supports.get(Support.DOWN_SIDE), new I3DOperation[] { LogisticsNewRenderPipe.basicTexture }));
							break;
						case WEST:
							objectsToRender.add(new Pair<IModel3D, I3DOperation[]>(LogisticsNewRenderPipe.supports.get(Support.UP_UP), new I3DOperation[] { LogisticsNewRenderPipe.basicTexture }));
							objectsToRender.add(new Pair<IModel3D, I3DOperation[]>(LogisticsNewRenderPipe.supports.get(Support.DOWN_UP), new I3DOperation[] { LogisticsNewRenderPipe.basicTexture }));
							objectsToRender.add(new Pair<IModel3D, I3DOperation[]>(LogisticsNewRenderPipe.supports.get(Support.NORTH_UP), new I3DOperation[] { LogisticsNewRenderPipe.basicTexture }));
							objectsToRender.add(new Pair<IModel3D, I3DOperation[]>(LogisticsNewRenderPipe.supports.get(Support.SOUTH_UP), new I3DOperation[] { LogisticsNewRenderPipe.basicTexture }));
							break;
						default:
							break;
					}
				}
			}
		}

		boolean solidSides[] = new boolean[6];
		for (EnumFacing dir : UtilEnumFacing.VALID_DIRECTIONS) {
			LPPosition pos = new LPPosition((TileEntity) pipeTile);
			pos.moveForward(dir);
			Block blockSide = pos.getBlock(pipeTile.getWorld());
			if (blockSide == null || !blockSide.isSideSolid(pipeTile.getWorld(), pos, dir.getOpposite()) || renderState.pipeConnectionMatrix.isConnected(dir)) {
				Iterator<Mount> iter = mountCanidates.iterator();
				while (iter.hasNext()) {
					Mount mount = iter.next();
					if (mount.dir == dir) {
						iter.remove();
					}
				}
			} else {
				solidSides[dir.ordinal()] = true;
			}
		}

		if (!mountCanidates.isEmpty()) {
			if (solidSides[EnumFacing.DOWN.ordinal()]) {
				findOponentOnSameSide(mountCanidates, EnumFacing.DOWN);
			} else if (solidSides[EnumFacing.UP.ordinal()]) {
				findOponentOnSameSide(mountCanidates, EnumFacing.UP);
			} else {
				removeFromSide(mountCanidates, EnumFacing.DOWN);
				removeFromSide(mountCanidates, EnumFacing.UP);
				if (mountCanidates.size() > 2) {
					removeIfHasOponentSide(mountCanidates);
				}
				if (mountCanidates.size() > 2) {
					removeIfHasConnectedSide(mountCanidates);
				}
				if (mountCanidates.size() > 2) {
					findOponentOnSameSide(mountCanidates, mountCanidates.get(0).dir);
				}
			}

			if (LPConstants.DEBUG && mountCanidates.size() > 2) {
				new RuntimeException("Trying to render " + mountCanidates.size() + " Mounts").printStackTrace();
			}

			for (Mount mount : mountCanidates) {
				objectsToRender.add(new Pair<IModel3D, I3DOperation[]>(LogisticsNewRenderPipe.mounts.get(mount), new I3DOperation[] { LogisticsNewRenderPipe.basicTexture }));
			}
		}

		for (EnumFacing dir : UtilEnumFacing.VALID_DIRECTIONS) {
			if (!renderState.pipeConnectionMatrix.isConnected(dir)) {
				for (IModel3D model : LogisticsNewRenderPipe.texturePlate_Outer.get(dir)) {
					IIconTransformation icon = Textures.LPnewPipeIconProvider.getIcon(renderState.textureMatrix.getTextureIndex());
					if (icon != null) {
						objectsToRender.add(new Pair<IModel3D, I3DOperation[]>(model, new I3DOperation[] { icon }));
					}
				}
			}
		}
		if (renderState.textureMatrix.isFluid()) {
			for (EnumFacing dir : UtilEnumFacing.VALID_DIRECTIONS) {
				if (!renderState.pipeConnectionMatrix.isConnected(dir)) {
					for (IModel3D model : LogisticsNewRenderPipe.texturePlate_Inner.get(dir)) {
						objectsToRender.add(new Pair<IModel3D, I3DOperation[]>(model, new I3DOperation[] { LogisticsNewRenderPipe.glassCenterTexture }));
					}
				} else {
					if (!renderState.textureMatrix.isRoutedInDir(dir)) {
						for (IModel3D model : LogisticsNewRenderPipe.sideTexturePlate.get(dir).getValue1()) {
							objectsToRender.add(new Pair<IModel3D, I3DOperation[]>(model, new I3DOperation[] { LogisticsNewRenderPipe.basicTexture }));
						}
					}
				}
			}
		}
	}

	private void findOponentOnSameSide(List<Mount> mountCanidates, EnumFacing dir) {
		boolean sides[] = new boolean[6];
		Iterator<Mount> iter = mountCanidates.iterator();
		while (iter.hasNext()) {
			Mount mount = iter.next();
			if (mount.dir != dir) {
				iter.remove();
			} else {
				sides[mount.side.ordinal()] = true;
			}
		}
		if (mountCanidates.size() <= 2) {
			return;
		}
		List<EnumFacing> keep = new ArrayList<EnumFacing>();
		if (sides[2] && sides[3]) {
			keep.add(EnumFacing.NORTH);
			keep.add(EnumFacing.SOUTH);
		} else if (sides[4] && sides[5]) {
			keep.add(EnumFacing.EAST);
			keep.add(EnumFacing.WEST);
		} else if (sides[0] && sides[1]) {
			keep.add(EnumFacing.UP);
			keep.add(EnumFacing.DOWN);
		}
		iter = mountCanidates.iterator();
		while (iter.hasNext()) {
			Mount mount = iter.next();
			if (!keep.contains(mount.side)) {
				iter.remove();
			}
		}
	}

	private void removeFromSide(List<Mount> mountCanidates, EnumFacing dir) {
		Iterator<Mount> iter = mountCanidates.iterator();
		while (iter.hasNext()) {
			Mount mount = iter.next();
			if (mount.dir == dir) {
				iter.remove();
			}
		}
	}

	private void reduceToOnePerSide(List<Mount> mountCanidates, EnumFacing dir, EnumFacing pref) {
		boolean found = false;
		Iterator<Mount> iter = mountCanidates.iterator();
		while (iter.hasNext()) {
			Mount mount = iter.next();
			if (mount.dir != dir) {
				continue;
			}
			if (mount.side == pref) {
				found = true;
			}
		}
		if (!found) {
			reduceToOnePerSide(mountCanidates, dir);
		} else {
			iter = mountCanidates.iterator();
			while (iter.hasNext()) {
				Mount mount = iter.next();
				if (mount.dir != dir) {
					continue;
				}
				if (mount.side != pref) {
					iter.remove();
				}
			}
		}
	}

	private void reduceToOnePerSide(List<Mount> mountCanidates, EnumFacing dir) {
		boolean found = false;
		Iterator<Mount> iter = mountCanidates.iterator();
		while (iter.hasNext()) {
			Mount mount = iter.next();
			if (mount.dir != dir) {
				continue;
			}
			if (found) {
				iter.remove();
			} else {
				found = true;
			}
		}
	}

	private void removeIfHasOponentSide(List<Mount> mountCanidates) {
		boolean sides[] = new boolean[6];
		Iterator<Mount> iter = mountCanidates.iterator();
		while (iter.hasNext()) {
			Mount mount = iter.next();
			sides[mount.dir.ordinal()] = true;
		}
		if (sides[2] && sides[3]) {
			removeFromSide(mountCanidates, EnumFacing.EAST);
			removeFromSide(mountCanidates, EnumFacing.WEST);
			reduceToOnePerSide(mountCanidates, EnumFacing.NORTH);
			reduceToOnePerSide(mountCanidates, EnumFacing.SOUTH);
		} else if (sides[4] && sides[5]) {
			removeFromSide(mountCanidates, EnumFacing.NORTH);
			removeFromSide(mountCanidates, EnumFacing.SOUTH);
			reduceToOnePerSide(mountCanidates, EnumFacing.EAST);
			reduceToOnePerSide(mountCanidates, EnumFacing.WEST);
		}
	}

	private void removeIfHasConnectedSide(List<Mount> mountCanidates) {
		boolean sides[] = new boolean[6];
		Iterator<Mount> iter = mountCanidates.iterator();
		while (iter.hasNext()) {
			Mount mount = iter.next();
			sides[mount.dir.ordinal()] = true;
		}
		for (int i = 2; i < 6; i++) {
			EnumFacing dir = UtilEnumFacing.getOrientation(i);
			EnumFacing rot = dir.getRotation(EnumFacing.UP);
			if (sides[dir.ordinal()] && sides[rot.ordinal()]) {
				reduceToOnePerSide(mountCanidates, dir, dir.getRotation(EnumFacing.DOWN));
				reduceToOnePerSide(mountCanidates, rot, rot.getRotation(EnumFacing.UP));
			}
		}
	}
}
