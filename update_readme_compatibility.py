#!/usr/bin/env python3

import json
import os
import re

MANIFEST_PATH = "app/src/main/assets/compatibility_manifest.json"
README_PATH = "README.md"

def load_manifest():
    with open(MANIFEST_PATH, 'r') as f:
        return json.load(f)

def generate_markdown(manifest):
    rules = manifest.get("rules", [])
    
    headers = ["OS Name", "SDK Version", "Model", "Status", "Unsupported Styles", "Notes"]
    rows = []
    
    status_icons = {
        "FULLY_SUPPORTED": "✅ Fully Supported",
        "PARTIAL_SUPPORT": "⚠️ Partial Support",
        "INCOMPATIBLE": "❌ Incompatible",
        "UNKNOWN": "❓ Unknown"
    }
    
    for rule in rules:
        status_raw = rule.get("status", "UNKNOWN")
        status = status_icons.get(status_raw, status_raw)
        
        match = rule.get("match", {})
        os_name_val = match.get("os_name", "Any")
        if isinstance(os_name_val, list):
            os_name = ", ".join(os_name_val)
        else:
            os_name = os_name_val or "Any"
        
        model = match.get("model", "Any") or "Any"
        
        min_sdk = match.get("min_sdk_version")
        max_sdk = match.get("max_sdk_version")
        
        if min_sdk and max_sdk:
            sdk = f"{min_sdk} - {max_sdk}"
        elif min_sdk:
            sdk = f">= {min_sdk}"
        elif max_sdk:
            sdk = f"<= {max_sdk}"
        else:
            sdk = "Any"
            
        styles = ", ".join(rule.get("unsupported_styles", []))
        desc = rule.get("description", "")
        
        rows.append([os_name, sdk, model, status, styles, desc])
        
    # Calculate column widths
    col_widths = [len(h) for h in headers]
    for row in rows:
        for i, cell in enumerate(row):
            col_widths[i] = max(col_widths[i], len(cell))
            
    lines = []
    
    # Format header
    header_line = "| " + " | ".join(h.ljust(w) for h, w in zip(headers, col_widths)) + " |"
    lines.append(header_line)
    
    # Format separator
    sep_line = "|-" + "-|-".join("-" * w for w in col_widths) + "-|"
    lines.append(sep_line)
    
    # Format rows
    for row in rows:
        row_line = "| " + " | ".join(c.ljust(w) for c, w in zip(row, col_widths)) + " |"
        lines.append(row_line)
    
    lines.append("")
    return "\n".join(lines)

def update_readme(new_content):
    with open(README_PATH, 'r') as f:
        content = f.read()
        
    # Replace between markers
    pattern = r'(<!-- COMPATIBILITY_START -->\n).*?(<!-- COMPATIBILITY_END -->)'
    replacement = f"\\g<1>{new_content}\n\\g<2>"
    
    new_readme = re.sub(pattern, replacement, content, flags=re.DOTALL)
    
    with open(README_PATH, 'w') as f:
        f.write(new_readme)

if __name__ == "__main__":
    print("Reading compatibility manifest...")
    manifest = load_manifest()
    
    print("Generating Markdown tables...")
    md_content = generate_markdown(manifest)
    
    print("Updating README.md...")
    update_readme(md_content)
    
    print("Done!")
