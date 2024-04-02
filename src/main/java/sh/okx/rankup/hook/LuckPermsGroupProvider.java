package sh.okx.rankup.hook;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import sh.okx.rankup.RankupPlugin;
import sh.okx.rankup.ranks.Rank;
import sh.okx.rankup.ranks.RankElement;

import java.util.UUID;

public class LuckPermsGroupProvider implements GroupProvider {

  private final LuckPerms luckPerms;
  private final ContextSet baseContextSet; // OMNI

  public LuckPermsGroupProvider(LuckPerms luckPerms, ContextSet contextSet) {
    this.luckPerms = luckPerms;
    this.baseContextSet = contextSet; // OMNI
  }

  public static LuckPermsGroupProvider createFromString(LuckPerms luckPerms, String context) {
    try {
      ImmutableContextSet.Builder builder = ImmutableContextSet.builder();
      for (String contextPair : context.split(" ")) {
        String[] keyValue = contextPair.split("=", 2);
        if (keyValue.length == 2) {
          builder.add(keyValue[0], keyValue[1]);
        }
      }

      return new LuckPermsGroupProvider(luckPerms, builder.build());
    } catch (NullPointerException | IllegalArgumentException ex) {
      throw new IllegalArgumentException("Context is invalid: " + context, ex);
    }
  }

  // START OMNI
  public ContextSet getPlayerSpecificContext(UUID uuid) {
    ImmutableContextSet.Builder builder = ImmutableContextSet.builder();
    builder.addAll(baseContextSet);

    // Omniverse Skyblock integration
    if (Bukkit.getServer().getPluginManager().isPluginEnabled("Skyblock")) {
      String profile = OmniverseSkyblockHook.getProfile(uuid);
      Bukkit.getLogger().info("Profile: " + profile);
      if (profile != null) {;
        builder.add("profile", profile);
      }
    }

    return builder.build();
  }

  public boolean doIgnoreContext(String rank) {
    RankElement<Rank> rankElement = RankupPlugin.rankupPluginInstance.getRankups().getByName(rank);
    return rankElement.getRank().isIgnoreContext();
  }
  // END OMNI

  @Override
  public boolean inGroup(UUID uuid, String group) {
    User user = luckPerms.getUserManager().getUser(uuid);
    ContextSet contextSet = !doIgnoreContext(group) ? getPlayerSpecificContext(uuid) : ImmutableContextSet.builder().build(); // OMNI
    for (Group lpGroup : user.getInheritedGroups(user.getQueryOptions().toBuilder().context(contextSet).build())) {
      if (lpGroup.getName().equalsIgnoreCase(group)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void addGroup(UUID uuid, String group) {
    User user = luckPerms.getUserManager().getUser(uuid);
    ContextSet contextSet = getPlayerSpecificContext(uuid); // OMNI
    user.data().add(InheritanceNode.builder(group).context(contextSet).build());

    luckPerms.getUserManager().saveUser(user);
  }

  @Override
  public void removeGroup(UUID uuid, String group) {
    User user = luckPerms.getUserManager().getUser(uuid);
    ContextSet contextSet = getPlayerSpecificContext(uuid); // OMNI
    user.data().remove(InheritanceNode.builder(group).context(contextSet).build());

    luckPerms.getUserManager().saveUser(user);
  }
}
