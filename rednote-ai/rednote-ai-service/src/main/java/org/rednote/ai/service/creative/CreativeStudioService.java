package org.rednote.ai.service.creative;

import org.rednote.ai.api.dto.CreativeDraftGenerateRequestDTO;
import org.rednote.ai.api.dto.CreativeOutlineGenerateRequestDTO;
import org.rednote.ai.api.dto.CreativeProjectCreateResponseDTO;
import org.rednote.ai.api.dto.CreativeProjectDocsUpsertRequestDTO;
import org.rednote.ai.api.vo.CreativeDraftVO;
import org.rednote.ai.api.vo.CreativeOutlineVO;
import org.rednote.ai.api.vo.CreativeProjectDocsUpsertVO;

public interface CreativeStudioService {

    CreativeProjectCreateResponseDTO createProject();

    CreativeProjectDocsUpsertVO upsertDocs(String projectId, CreativeProjectDocsUpsertRequestDTO request);

    CreativeOutlineVO generateOutline(CreativeOutlineGenerateRequestDTO request);

    CreativeDraftVO generateDraft(CreativeDraftGenerateRequestDTO request);

    void cleanup(String projectId);
}
