package com.enderio.core.common.recipes;

import com.google.gson.*;
import net.minecraft.fluid.Fluid;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.ITag;
import net.minecraft.tags.TagCollectionManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

public class EnderFluidIngredient implements Predicate<FluidStack> {

  private int amount;
  private List<IFluidList> acceptedFluids = new ArrayList<>();

  public EnderFluidIngredient(int amount, Fluid... fluids) {
    for (Fluid fluid : fluids) {
      acceptedFluids.add(new SingleFluidList(fluid));
    }
    this.amount = amount;
  }

  public EnderFluidIngredient(int amount, ITag<Fluid>... fluids) {
    for (ITag<Fluid> fluid : fluids) {
      acceptedFluids.add(new TagList(fluid));
    }
    this.amount = amount;
  }

  public JsonElement serialize() {
    return Serializer.INSTANCE.serialize(this);
  }


  private EnderFluidIngredient(int amount) {
    this.amount = amount;
  }

  public static EnderFluidIngredient merge(int amount, EnderFluidIngredient... fluidIngredients) {
    EnderFluidIngredient returnFluidIngredient = new EnderFluidIngredient(amount);
    for (EnderFluidIngredient fluidIngredient: fluidIngredients) {
      returnFluidIngredient.acceptedFluids.addAll(fluidIngredient.acceptedFluids);
    }
    return returnFluidIngredient;
  }

  @Override
  public boolean test(FluidStack fluidStack) {
    for (IFluidList fluidList: acceptedFluids) {
      if (fluidList.getFluids().contains(fluidStack.getFluid()) && amount <= fluidStack.getAmount()) {
        return true;
      }
    }
    return false;
  }

  private static class SingleFluidList implements IFluidList {
    private final Fluid fluid;

    SingleFluidList(Fluid fluid) {
      this.fluid = fluid;
    }

    @Override
    public Collection<Fluid> getFluids() {
      return Collections.singleton(fluid);
    }

    @Override
    public JsonObject serialize() {
      JsonObject jsonobject = new JsonObject();
      jsonobject.addProperty("fluid", fluid.getRegistryName().toString());
      return jsonobject;
    }
  }

  private static class TagList implements IFluidList {
    private final ITag<Fluid> fluidTag;

    private TagList(ITag<Fluid> fluidTag) {
      this.fluidTag = fluidTag;
    }

    @Override
    public Collection<Fluid> getFluids() {
      return fluidTag.getAllElements();
    }

    @Override
    public JsonObject serialize() {
      JsonObject jsonobject = new JsonObject();
      jsonobject.addProperty("tag", TagCollectionManager.getManager().getFluidTags().getValidatedIdFromTag(fluidTag).toString());
      return jsonobject;
    }
  }

  public Serializer getSerializer() {
    return EnderFluidIngredient.Serializer.INSTANCE;
  }

  public static class Serializer {
    public static final EnderFluidIngredient.Serializer INSTANCE = new EnderFluidIngredient.Serializer();
    private Serializer() {
    }

    public EnderFluidIngredient parse(JsonObject json) {
      EnderFluidIngredient fluidIngredient = new EnderFluidIngredient(json.get("amount").getAsInt());
      JsonElement fluids = json.get("fluids");
      if (fluids.isJsonObject()) {
        fluidIngredient.acceptedFluids.add(deserializeFluidList(fluids.getAsJsonObject()));
        return fluidIngredient;
      } else if (fluids.isJsonArray()) {
        JsonArray jsonarray = fluids.getAsJsonArray();
        StreamSupport.stream(jsonarray.spliterator(), false).forEachOrdered(jsonItem ->
           fluidIngredient.acceptedFluids.add(deserializeFluidList(JSONUtils.getJsonObject(jsonItem, "item")))
        );
        return fluidIngredient;
      }
      throw new JsonSyntaxException("could not finish parsing of FluidIngredient");
    }

    public JsonObject serialize(EnderFluidIngredient fluidIngredient) {
      JsonObject json = new JsonObject();
      if (fluidIngredient.acceptedFluids.size() == 1) {
        json.add("fluids", fluidIngredient.acceptedFluids.get(0).serialize());
      } else {
        JsonArray jsonarray = new JsonArray();
        for (IFluidList acceptedFluid: fluidIngredient.acceptedFluids) {
          jsonarray.add(acceptedFluid.serialize());
        }
        json.add("fluids", jsonarray);
      }
      json.addProperty("amount", fluidIngredient.amount);
      return json;
    }

    public EnderFluidIngredient parse(PacketBuffer buffer) {
      EnderFluidIngredient fluidIngredient = new EnderFluidIngredient(buffer.readInt());
      int numFluids = buffer.readInt();
      for (int i = 0; i < numFluids; i++) {
        fluidIngredient.acceptedFluids.add(new SingleFluidList(ForgeRegistries.FLUIDS.getValue(buffer.readResourceLocation())));
      }
      return fluidIngredient;
    }

    public void write(PacketBuffer buffer, EnderFluidIngredient ingredient) {
      buffer.writeInt(ingredient.amount);
      buffer.writeInt((int)ingredient.acceptedFluids.stream().flatMap(fluidList -> fluidList.getFluids().stream()).count());
      ingredient.acceptedFluids.stream().flatMap(fluidList -> fluidList.getFluids().stream()).forEach(fluid -> buffer.writeResourceLocation(fluid.getRegistryName()));
    }
  }

  private static IFluidList deserializeFluidList(JsonObject json) {
    ResourceLocation resourcelocation;
    if (json.has("fluid")) {
      resourcelocation = new ResourceLocation(JSONUtils.getString(json, "fluid"));
      Fluid fluid = ForgeRegistries.FLUIDS.getValue(resourcelocation);
      if (fluid == null) {
          throw  new JsonSyntaxException("Unknown fluid '" + resourcelocation + "'");
      }
      return new SingleFluidList(fluid);
    } else if (json.has("tag")) {
      resourcelocation = new ResourceLocation(JSONUtils.getString(json, "tag"));
      ITag<Fluid> itag = TagCollectionManager.getManager().getFluidTags().get(resourcelocation);
      if (itag == null) {
        throw new JsonSyntaxException("Unknown fluid tag '" + resourcelocation + "'");
      } else {
        return new TagList(itag);
      }
    } else {
      throw new JsonParseException("An ingredient entry needs either a tag or an item");
    }

  }

  private interface IFluidList {
    Collection<Fluid> getFluids();

    JsonObject serialize();
  }
}
