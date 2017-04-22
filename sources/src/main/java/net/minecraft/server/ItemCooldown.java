package net.minecraft.server;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class ItemCooldown {

	public final Map<Item, ItemCooldown.Info> a = Maps.newHashMap(); // PAIL: private -> public
	public int b; // PAIL: currentTick, private -> public

    public ItemCooldown() {}

    public boolean a(Item item) {
        return this.a(item, 0.0F) > 0.0F;
    }

    public float a(Item item, float f) {
        ItemCooldown.Info itemcooldown_info = this.a.get(item);

        if (itemcooldown_info != null) {
            float f1 = itemcooldown_info.b - itemcooldown_info.a;
            float f2 = itemcooldown_info.b - (this.b + f);

            return MathHelper.a(f2 / f1, 0.0F, 1.0F);
        } else {
            return 0.0F;
        }
    }

    public void a() {
        ++this.b;
        if (!this.a.isEmpty()) {
            Iterator iterator = this.a.entrySet().iterator();

            while (iterator.hasNext()) {
                Entry entry = (Entry) iterator.next();

                if (((ItemCooldown.Info) entry.getValue()).b <= this.b) {
                    iterator.remove();
                    this.c((Item) entry.getKey());
                }
            }
        }

    }

    public void a(Item item, int i) {
        this.a.put(item, new ItemCooldown.Info(this.b, this.b + i, null));
        this.b(item, i);
    }

    protected void b(Item item, int i) {}

    protected void c(Item item) {}

    public class Info { // PAIL: private -> public

        final int a;
        public final int b; // PAIL: endTick, private -> public

        private Info(int i, int j) {
            this.a = i;
            this.b = j;
        }

        Info(int i, int j, Object object) {
            this(i, j);
        }
    }
}
