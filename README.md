# 🔧 MMOItemsRepair Plugin Documentation

## 📋 Table of Contents
1. [Overview](#overview)
2. [Installation](#installation)
3. [Commands](#commands)
4. [Permissions](#permissions)

---

## 📖 Overview

**MMOItemsRepair** is a specialized Minecraft plugin that provides an interactive repair system for MMOItems. It features a custom GUI interface, tier-based repair costs, and seamless integration with the MMOItems plugin.

---

## 🚀 Installation

### Prerequisites
- **Minecraft Server**: Paper/Spigot 1.21.4+
- **Java Version**: Java 21+
- **Required Plugin**: MMOItems 6.10.1+
- **Optional**: MMOCore (recommended for full MMOItems functionality)

### Installation Steps

1. **Download Dependencies**
   ```bash
   # Ensure these plugins are installed:
   - MMOItems 6.10.1+
   - MMOCore (optional but recommended)
   ```

2. **Install Plugin**
   ```bash
   # Place MMOItemsRepair-1.0.0.jar in your plugins folder
   /server/plugins/MMOItemsRepair-1.0.0.jar
   ```

3. **Start Server**
   ```bash
   # Plugin will auto-generate configuration files:
   - config.yml
   - language.yml
   ```

4. **Configure Repair Material**
   ```yaml
   # Edit config.yml to set your repair material
   repair:
     material-id: 'material.YOUR_REPAIR_ITEM'
   ```

5. **Restart or Reload**
   ```bash
   /mmoitemsrepair reload
   ```

---

## 🎮 Commands

### Player Commands

| Command | Description | Permission | Aliases |
|---------|-------------|------------|---------|
| `/repair` | Open repair menu for held MMOItem | `mmoitemsrepair.use` | `/fix`, `/mmorepair` |

### Admin Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/mmoitemsrepair reload` | Reload all configuration files | `mmoitemsrepair.admin` |
| `/mmoitemsrepair version` | Show plugin version and info | None |
| `/mmoitemsrepair info` | Display plugin information | None |
| `/mmoitemsrepair debug <on\|off>` | Toggle debug mode | `mmoitemsrepair.admin` |
| `/mmoitemsrepair help` | Show help message | None |

### Debug Commands (Admin Only)

| Command | Description | Permission |
|---------|-------------|------------|
| `/mmoitemsrepair tierdebug` | Debug tier detection for held item | `mmoitemsrepair.admin` |
| `/mmoitemsrepair testmultiplier` | Test tier multiplier for held item | `mmoitemsrepair.admin` |

### Command Examples

```bash
# Basic repair usage
/repair

# Admin management
/mmoitemsrepair reload
/mmoitemsrepair debug on

# Debugging tier issues
/mmoitemsrepair tierdebug
/mmoitemsrepair testmultiplier
```

---

## 🔐 Permissions

### Basic Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `mmoitemsrepair.use` | Use the repair command | `true` |
| `mmoitemsrepair.admin` | Access admin commands | `op` |

### Wildcard Permission

| Permission | Description | Default |
|------------|-------------|---------|
| `mmoitemsrepair.*` | All plugin permissions | `op` |

### Permission Setup Examples

```yaml
# LuckPerms examples
/lp group default permission set mmoitemsrepair.use true
/lp group admin permission set mmoitemsrepair.admin true

# GroupManager examples
- mmoitemsrepair.use    # For default players
- mmoitemsrepair.admin  # For administrators
```

---

### Version Compatibility
| Plugin Version | MMOItems Version | Minecraft Version |
|----------------|------------------|-------------------|
| 1.0.0 | 6.10.1+ | 1.21.4+ |

---

*Documentation last updated for MMOItemsRepair v1.0.0*
