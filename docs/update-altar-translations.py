"""Keep the altar's compact UI labels consistent across the six supported locales."""
import json
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]/'common/src/main/resources/assets/kitsunebi/lang'
keys=['input','color','shape','preview','output','quantity','craft','ready','missing','output_full',
      'ingredient.light','ingredient.amethyst','ingredient.bamboo','ingredient.lapis','ingredient.dye',
      'ingredient.fire','ingredient.amethyst_block','ingredient.block','ingredient.slime','ingredient.soul',
      'ingredient.phantom','ingredient.half','ingredient.quarter','ingredient.stick']
rows={
 'ja_jp':['素材投入','色を選択','形状を選択','完成品','取出し','完成個数','製作する','製作できます','素材が不足しています','取出し欄に空きが必要',
          '発光素材（粉／イカスミ）','アメジストの欠片','竹','ラピスラズリ','対応する染料','狐火（各色混在可）','アメジストブロック','狐火ブロック','スライム／ハチミツ','ソウルサンド／ソイル','幻影ブロック','幻影ハーフ（横／縦）','幻影クォーター（横／縦）','棒'],
 'en_us':['Materials','Color','Shape','Preview','Output','Output quantity','Craft','Ready to craft','Missing materials','Make room in output',
          'Glow dust / ink','Amethyst shard','Bamboo','Lapis lazuli','Selected dye','Foxfire (any color)','Amethyst block','Foxfire block','Slime / honey','Soul sand / soil','Phantom block','Phantom half (H/V)','Phantom quarter (H/V)','Stick'],
 'ko_kr':['재료 투입','색상 선택','형태 선택','미리 보기','완성품','완성 수량','제작','제작 가능','재료 부족','완성품 공간 필요',
          '발광 가루 / 먹물','자수정 조각','대나무','청금석','해당 염료','여우불 (색 혼합 가능)','자수정 블록','여우불 블록','슬라임 / 꿀','영혼 모래 / 흙','환영 블록','환영 반 블록 (가로/세로)','환영 1/4 블록 (가로/세로)','막대기'],
 'zh_cn':['投入材料','选择颜色','选择形状','预览','成品','成品数量','制作','可以制作','材料不足','请腾出成品空间',
          '荧石粉／荧光墨囊','紫水晶碎片','竹子','青金石','对应染料','狐火（可混色）','紫水晶块','狐火方块','黏液／蜂蜜','灵魂沙／灵魂土','幻影方块','幻影半砖（横／竖）','幻影四分之一砖（横／竖）','木棍'],
 'zh_tw':['投入材料','選擇顏色','選擇形狀','預覽','成品','成品數量','製作','可以製作','材料不足','請騰出成品空間',
          '螢石粉／螢光墨囊','紫水晶碎片','竹子','青金石','對應染料','狐火（可混色）','紫水晶方塊','狐火方塊','史萊姆／蜂蜜','靈魂砂／靈魂土','幻影方塊','幻影半磚（橫／直）','幻影四分之一磚（橫／直）','木棒'],
 'zh_hk':['投入材料','選擇顏色','選擇形狀','預覽','成品','成品數量','製作','可以製作','材料不足','請騰出成品空間',
          '螢石粉／螢光墨囊','紫水晶碎片','竹','青金石','對應染料','狐火（可混色）','紫水晶方塊','狐火方塊','史萊姆／蜂蜜','靈魂沙／靈魂土','幻影方塊','幻影半磚（橫／直）','幻影四分之一磚（橫／直）','木棒']}
for locale,values in rows.items():
    assert len(keys)==len(values)
    path=ROOT/(locale+'.json')
    data=json.loads(path.read_text(encoding='utf-8'))
    # These names now come from the selected vanilla item, including both alternatives.
    vanilla_names={'ingredient.light','ingredient.dye','ingredient.slime','ingredient.soul'}
    for key in vanilla_names: data.pop('gui.kitsunebi.altar.'+key,None)
    data.update({'gui.kitsunebi.altar.'+k:v for k,v in zip(keys,values) if k not in vanilla_names})
    category_labels={
        'ja_jp':['幻影ブロック','装備・ツール','狐火を選択','装備を選択','追加予定の枠','準備中','装備・ツールは次の仕様で追加予定です。'],
        'en_us':['Phantom Blocks','Gear & Tools','Select foxfire','Select gear','Reserved slot','Coming soon','Equipment and tools will be added in the next specification.'],
        'ko_kr':['환영 블록','장비·도구','여우불 선택','장비 선택','추가 예정 슬롯','준비 중','장비와 도구는 다음 사양에서 추가됩니다.'],
        'zh_cn':['幻影方块','装备与工具','选择狐火','选择装备','预留栏位','敬请期待','装备与工具将在后续规格中添加。'],
        'zh_tw':['幻影方塊','裝備與工具','選擇狐火','選擇裝備','預留欄位','敬請期待','裝備與工具將於後續規格中加入。'],
        'zh_hk':['幻影方塊','裝備與工具','選擇狐火','選擇裝備','預留欄位','敬請期待','裝備與工具將於後續規格中加入。']
    }[locale]
    new_keys=['category.kitsunebi.phantom','category.kitsunebi.equipment']+['gui.kitsunebi.altar.'+k for k in ['fire_select','equipment_select','placeholder','coming_soon','next_update']]
    data.update(dict(zip(new_keys,category_labels)))
    path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print('Updated altar UI translations in all six locales.')
