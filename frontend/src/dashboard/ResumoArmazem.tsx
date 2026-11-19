import type { Ingrediente, VolumePorTipo } from '@/api'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { COMPARTIMENTOS, TIPOS, formatarQuantidade, nomeTipo, tipoInfo } from '@/format'

type ResumoArmazemProps = {
  ingredientes: Ingrediente[]
  volume: VolumePorTipo[]
}

export function ResumoArmazem({ ingredientes, volume }: ResumoArmazemProps) {
  return (
    <div className="grid gap-6">
      <section className="grid gap-3 sm:grid-cols-3">
        {TIPOS.map((tipo) => {
          const total = volume.find((item) => item.type === tipo.id)?.totalQuantity ?? 0
          return (
            <Card key={tipo.id} size="sm">
              <CardHeader>
                <CardDescription>{tipo.nome}</CardDescription>
                <CardTitle className="text-2xl">{formatarQuantidade(total, tipo.id)}</CardTitle>
              </CardHeader>
              <CardContent className="text-xs text-muted-foreground">
                Até {tipo.capacidade} {tipo.unidade} em cada compartimento
              </CardContent>
            </Card>
          )
        })}
      </section>

      <section className="grid gap-3 sm:grid-cols-5">
        {COMPARTIMENTOS.map((compartimento) => {
          const itens = ingredientes.filter((item) => item.compartimentoId === compartimento.id)
          const saldo = itens.reduce((total, item) => total + item.quantidade, 0)
          const tipo = itens.find((item) => item.quantidade > 0)?.tipo ?? itens[0]?.tipo ?? null
          const info = tipoInfo(tipo)
          const ocupacao = info && info.capacidade > 0 ? Math.min(100, (saldo / info.capacidade) * 100) : 0
          return (
            <Card key={compartimento.id} size="sm">
              <CardHeader>
                <CardTitle>{compartimento.codigo}</CardTitle>
                <CardDescription>{tipo ? nomeTipo(tipo) : 'Vazio'}</CardDescription>
              </CardHeader>
              <CardContent className="grid gap-2">
                <p className="text-sm font-medium">{tipo ? formatarQuantidade(saldo, tipo) : 'Livre'}</p>
                <div className="h-1.5 overflow-hidden rounded-full bg-muted">
                  <div className="h-full rounded-full bg-foreground" style={{ width: `${ocupacao}%` }} />
                </div>
              </CardContent>
            </Card>
          )
        })}
      </section>
    </div>
  )
}
