/*
 * Copyright 2017 Aljoscha Grebe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.almightyalpaca.intellij.plugins.discord.data;

import com.almightyalpaca.intellij.plugins.discord.collections.cloneable.CloneableCollections;
import com.almightyalpaca.intellij.plugins.discord.collections.cloneable.CloneableHashMap;
import com.almightyalpaca.intellij.plugins.discord.collections.cloneable.CloneableMap;
import com.almightyalpaca.intellij.plugins.discord.collections.cloneable.ReallyCloneable;
import com.almightyalpaca.intellij.plugins.discord.settings.DiscordIntegrationProjectSettings;
import com.almightyalpaca.intellij.plugins.discord.settings.data.ProjectSettings;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Objects;

public class ProjectInfo implements Serializable, ReallyCloneable<ProjectInfo>, Comparable<ProjectInfo>
{
    @NotNull
    private final String name;
    private final String id;
    @NotNull
    private final CloneableMap<String, FileInfo> files;
    private final long timeOpened;
    @NotNull
    private ProjectSettings<? extends ProjectSettings> settings;
    private long timeAccessed;

    public ProjectInfo(String id, @NotNull ProjectSettings<? extends ProjectSettings> settings, @NotNull String name, long timeOpened)
    {
        this(id, settings, name, timeOpened, timeOpened);
    }

    public ProjectInfo(String id, @NotNull ProjectSettings<? extends ProjectSettings> settings, @NotNull String name, long timeOpened, long timeAccessed)
    {
        this(id, settings, name, timeOpened, timeAccessed, new CloneableHashMap<>());
    }

    public ProjectInfo(String id, @NotNull ProjectSettings<? extends ProjectSettings> settings, @NotNull String name, long timeOpened, long timeAccessed, @NotNull CloneableMap<String, FileInfo> files)
    {
        this.settings = settings;
        this.timeOpened = timeOpened;
        this.timeAccessed = timeAccessed;
        this.name = name;
        this.id = id;
        this.files = files;
    }

    public ProjectInfo(@NotNull Project project)
    {
        this(project.getLocationHash(), DiscordIntegrationProjectSettings.getInstance(project).getSettings(), project.getName(), System.currentTimeMillis());
    }

    public long getTimeOpened()
    {
        return this.timeOpened;
    }

    public long getTimeAccessed()
    {
        return this.timeAccessed;
    }

    void setTimeAccessed(long timeAccessed)
    {
        this.timeAccessed = timeAccessed;
    }

    @NotNull
    public String getName()
    {
        return this.name;
    }

    @NotNull
    public String getId()
    {
        return this.id;
    }

    @NotNull
    public ProjectSettings<? extends ProjectSettings> getSettings()
    {
        return this.settings;
    }

    void setSettings(@NotNull ProjectSettings<? extends ProjectSettings> settings)
    {
        this.settings = settings;
    }

    @NotNull
    public CloneableMap<String, FileInfo> getFiles()
    {
        return CloneableCollections.unmodifiableCloneableMap(this.files);
    }

    @Override
    public int compareTo(@NotNull ProjectInfo project)
    {
        return Long.compare(this.timeAccessed, project.timeAccessed);
    }

    @Override
    public boolean equals(@Nullable Object obj)
    {
        return obj instanceof ProjectInfo && Objects.equals(this.id, ((ProjectInfo) obj).id);
    }

    @NotNull
    @Override
    public String toString()
    {
        return "ProjectInfo{" + "name='" + this.name + '\'' + ", id='" + this.id + '\'' + ", files=" + this.files + ", timeOpened=" + this.timeOpened + ", settings=" + this.settings + ", timeAccessed=" + this.timeAccessed + '}';
    }

    @NotNull
    @SuppressWarnings({"MethodDoesntCallSuperMethod", "CloneDoesntDeclareCloneNotSupportedException"})
    @Override
    public ProjectInfo clone()
    {
        return new ProjectInfo(this.id, this.settings.clone(), this.name, this.timeOpened, this.timeAccessed, this.files.clone());
    }

    void addFile(@NotNull FileInfo file)
    {
        this.files.put(file.getId(), file);
    }

    void removeFile(@NotNull String fileId)
    {
        this.files.remove(fileId);
    }
}
