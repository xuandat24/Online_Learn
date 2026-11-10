"""Mirror source into TEMP when Windows protected Documents blocks build tools."""
from pathlib import Path
import shutil, tempfile
root = Path(__file__).resolve().parents[1]
target = Path(tempfile.gettempdir()) / 'onlinelearn-build'
target.mkdir(exist_ok=True)
for name in ['backend', 'frontend', 'scripts']:
    shutil.copytree(root / name, target / name, dirs_exist_ok=True, ignore=shutil.ignore_patterns('node_modules','target','dist','test-results','playwright-report'))
print(target)
